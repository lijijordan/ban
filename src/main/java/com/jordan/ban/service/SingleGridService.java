package com.jordan.ban.service;

import com.jordan.ban.dao.SingleGridRepository;
import com.jordan.ban.domain.OrderRequest;
import com.jordan.ban.domain.OrderType;
import com.jordan.ban.entity.Grid;
import com.jordan.ban.entity.SingleGrid;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Gateio;
import com.jordan.ban.market.parser.MarketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SingleGridService {

    @Autowired
    private SingleGridRepository singleGridRepository;


    @Autowired
    private OrderService orderService;


//    public static final int SPLIT_COUNT = 100;
//    public static final float UP_PERCENT = 0.1f;
//    public static final float TOTAL_COIN = 7.1f;

    /**
     * * SumCoin/CurrentPrice + 1% + CurrentPrice + 2% ...
     * * FromPrice  = CurrentPrice;
     * * ToPrice = CurrentPrice * (100% + 10%?);
     * * GridCount = 100
     * * EachGridCoin = SumCoin/GridCount;
     * * EachPrice = CurrentPrice + 0.1%;
     *
     * @param splitCount   分割的网格数量
     * @param currentPrice 市价
     * @param symbol       交易对
     * @param upPercent    网格区间
     * @param totalCoin    准备交易的币量总数
     */
    public void generateSingleGrid(int splitCount, double currentPrice, String symbol, float upPercent, float totalCoin, String market) {
//        this.singleGridRepository.save(Grid.builder().symbol(symbol).low(low).high(high).quota(quota).volume(totalCoin * quota)
//                .lastVolume(totalCoin * quota).build());


        float eachGridCoin = totalCoin / splitCount;
        float eachUpPercent = upPercent / splitCount;

        double fromPrice = currentPrice;
        for (int i = 1; i <= splitCount; i++) {
            double toPrice = currentPrice * (1 + eachUpPercent * i);
//            SingleGrid singleGrid = SingleGrid.builder().high(toPrice).low(fromPrice)
//                    .quota(eachUpPercent).volume(eachGridCoin).symbol(symbol).build();
//            singleGridRepository.save(singleGrid);
            // place order
            orderService.crateSingleOrder(OrderRequest.builder()
                    .type(OrderType.SELL_LIMIT).amount(eachGridCoin).price(toPrice)
                    .symbol(symbol).build(), MarketFactory.getMarket(market));
            fromPrice = toPrice;
        }

    }
}
