package com.jordan.ban.service;

import com.jordan.ban.dao.GridRepository;
import com.jordan.ban.entity.Grid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jordan.ban.service.TradeService.MIN_TRADE_AMOUNT;

@Service
@Slf4j
public class GridService {
    @Autowired
    private GridRepository gridRepository;

    public void initGrid(String symbol, float low, float high, float quota, double totalCoin) {
        this.gridRepository.save(Grid.builder().symbol(symbol).low(low).high(high).quota(quota).volume(totalCoin * quota)
                .lastVolume(totalCoin * quota).build());
    }

    public double matchGrid(double diffPercent, double tradeVolume, Grid grid, String symbol) {
        grid = this.gridRepository.find(diffPercent, symbol);
        if (grid == null) {
            log.info("no grid");
            return 0;
        }
        double val = grid.getLastVolume(), result;
        if (tradeVolume < val) {
            result = tradeVolume;
            grid.setLastVolume(val - tradeVolume);
        } else {
            result = val;
            grid.setLastVolume(0);
        }

        if (result <= MIN_TRADE_AMOUNT) {
            log.info("trade volume：{} less than min trade volume，not deal！", result);
            return 0;
        }

        this.gridRepository.save(grid);
        return result;
    }
}
