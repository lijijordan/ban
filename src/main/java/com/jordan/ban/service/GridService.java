package com.jordan.ban.service;

import com.jordan.ban.dao.GridRepository;
import com.jordan.ban.domain.GridMatch;
import com.jordan.ban.entity.Grid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jordan.ban.service.TradeServiceETH.MIN_TRADE_AMOUNT;

@Service
@Slf4j
public class GridService {
    @Autowired
    private GridRepository gridRepository;

    public void initGrid(String symbol, float low, float high, float quota, double totalCoin) {
        log.info("Init grid web: {}%~{}% : {}%", low * 100, high * 100, quota * 100);
        this.gridRepository.save(Grid.builder().symbol(symbol).low(low).high(high).quota(quota).volume(totalCoin * quota)
                .lastVolume(totalCoin * quota).build());
    }

    public GridMatch matchGrid(double diffPercent, double tradeVolume, String symbol) {
        boolean isMatch;
        Grid grid = this.gridRepository.find(diffPercent, symbol);
        double result = 0;
        if (grid == null) {
            log.info("no grid");
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
            log.info("trade volume：{} less than min trade volume，not deal！", result);
            isMatch = false;
        }
        if (isMatch) {
            this.gridRepository.save(grid);
        }
        return GridMatch.builder().grid(grid).matchResult(result).isMatch(isMatch).build();
    }
}
