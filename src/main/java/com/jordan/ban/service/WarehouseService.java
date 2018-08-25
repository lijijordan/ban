package com.jordan.ban.service;

import com.jordan.ban.dao.GridOperationalLogRepository;
import com.jordan.ban.dao.GridRepository;
import com.jordan.ban.dao.WareHouseRepository;
import com.jordan.ban.domain.WareHouseState;
import com.jordan.ban.entity.Grid;
import com.jordan.ban.entity.GridOperationalLog;
import com.jordan.ban.entity.TradeRecord;
import com.jordan.ban.entity.WareHouse;
import com.jordan.ban.exception.TradeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class WarehouseService {

    @Autowired
    private WareHouseRepository wareHouseRepository;

    @Autowired
    private GridOperationalLogRepository gridOperationalLogRepository;

    @Autowired
    private GridRepository gridRepository;

    public double checkAndOutWareHouse(double comeDiffPercent, double comeVolume, String symbol) {
        double volume = 0;
        List<WareHouse> wareHouses = this.wareHouseRepository.findAllByStateIsNotAndSymbol(WareHouseState.out, symbol);
        wareHouses.sort((o1, o2) -> {
            if (o1.getDiffPercentOut() >= o2.getDiffPercentOut()) {
                return 1;
            } else {
                return -1;
            }
        });
        if (wareHouses != null && wareHouses.size() > 0) {
            for (int i = 0; i < wareHouses.size(); i++) {
                WareHouse wareHouse = wareHouses.get(i);
                // 进入购买位置
                if (comeDiffPercent > wareHouse.getDiffPercentOut()) {
                    double out = this.outWareHouse(comeVolume, wareHouse);
                    volume += out;
                    comeVolume = comeVolume - out;
                    if (comeVolume <= 0) {
                        break;
                    }
                }
            }
        }
        return volume;
    }

    private double outWareHouse(double comeVolume, WareHouse wareHouse) {
        double result = 0; // out volume
        log.debug("out warehouse:[{}],[{}]", comeVolume, wareHouse.getID());
        double leftVolume = wareHouse.getVolumeIn() - wareHouse.getVolumeOut();
        if (comeVolume >= leftVolume) {  // 剩余库存全部出去
            wareHouse.setState(WareHouseState.out);
            result = leftVolume;
        } else { // 部分出库
            wareHouse.setState(WareHouseState.partOut);
            result = comeVolume;
        }
        wareHouse.setVolumeOut(result + wareHouse.getVolumeOut());
        wareHouse.setTimeOut(new Date());
        this.wareHouseRepository.save(wareHouse);
        // 恢复网格数据
        Grid grid = this.gridRepository.findById(wareHouse.getGridId()).get();
        if (grid == null) {
            throw new TradeException("Can not find any grid!");
        }
        grid.setLastVolume(grid.getLastVolume() + result);
        this.gridRepository.save(grid);
        this.gridOperationalLogRepository.save(GridOperationalLog.builder().gridId(grid.getID()).matchVolume(result)
                .wareHouseId(wareHouse.getID()).operationalType(GridOperationalLog.OPERATIONAL_TYPE_PLUS).build());

        return result;
    }


    public WareHouse buildWareHouse(double eatPercent, double volume, double warehouseOutDiff, long gridId, String symbol) {
        double diffOut = (eatPercent * -1) + warehouseOutDiff;
        return wareHouseRepository.save(WareHouse.builder().diffPercentIn(eatPercent)
                .timeIn(new Date()).state(WareHouseState.in).volumeIn(volume).gridId(gridId)
                .diffPercentOut(diffOut).symbol(symbol)
                .build());
    }

}
