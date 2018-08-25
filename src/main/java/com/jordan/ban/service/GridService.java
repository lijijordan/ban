package com.jordan.ban.service;

import com.jordan.ban.dao.GridOperationalLogRepository;
import com.jordan.ban.dao.GridRepository;
import com.jordan.ban.domain.GridMatch;
import com.jordan.ban.domain.TradeDirect;
import com.jordan.ban.entity.Grid;
import com.jordan.ban.entity.GridOperationalLog;
import com.jordan.ban.entity.WareHouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.jordan.ban.service.TradeServiceETH.MIN_TRADE_AMOUNT;

@Service
@Slf4j
public class GridService {
    @Autowired
    private GridRepository gridRepository;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private GridOperationalLogRepository gridOperationalLogRepository;


    public void save(Grid grid) {
        this.gridRepository.save(grid);
    }

    public void initGrid(String symbol, float low, float high, float quota, double totalCoin) {
        log.debug("Init grid web: {}%~{}% : {}%", low * 100, high * 100, quota * 100);
        this.gridRepository.save(Grid.builder().symbol(symbol).low(low).high(high).quota(quota).volume(totalCoin * quota)
                .lastVolume(totalCoin * quota).build());
    }

    public GridMatch matchGrid(double diffPercent, double tradeVolume, String symbol, double warehouseOutDiff) {
        boolean isMatch;
        Grid grid = this.gridRepository.find(diffPercent, symbol);
        double result = 0;
        if (grid == null) {
            log.debug("no grid");
            isMatch = false;
        } else {
            isMatch = true;
            double val = grid.getLastVolume();
            if (tradeVolume < val) {
                result = tradeVolume;
                grid.setLastVolume(val - tradeVolume);
            } else {
                result = val;
                grid.setLastVolume(0);
            }
        }
        if (result <= MIN_TRADE_AMOUNT) {
            log.debug("trade volume：{} less than min trade volume，not deal！", result);
            isMatch = false;
        }
        if (isMatch) {
            log.debug("match grid update grid:{}", grid);
            grid.setTotalMatch(grid.getTotalMatch() + result);
            this.gridRepository.save(grid);

            // build warehouse
            WareHouse wareHouse = this.warehouseService.buildWareHouse(diffPercent, result,
                    warehouseOutDiff, grid.getID(), symbol);

            // record grid operation log.
            this.gridOperationalLogRepository.save(GridOperationalLog.builder().gridId(grid.getID())
                    .matchVolume(result).wareHouseId(wareHouse.getID()).operationalType(GridOperationalLog.OPERATIONAL_TYPE_LESS).build());

        }
        return GridMatch.builder().grid(grid).matchResult(result).isMatch(isMatch).build();
    }

    public List<Grid> findGridsBySymbol(String symbol) {
        return this.gridRepository.findAllBySymbol(symbol);
    }
}
