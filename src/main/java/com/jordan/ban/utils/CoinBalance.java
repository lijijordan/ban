package com.jordan.ban.utils;

public class CoinBalance {

    /**
     * warehouse-grid start:
     *  Dragonex full ETH;
     *  Fcoin full USDT
     * @param args
     */
    public static void main(String[] args) {

        double dragonETH = 6.3185;
        double dragonUSDT = 0;

        double fCoinETH = 1.2931458850527306;
        double fCoinUSDT = 411.225138880984645046;

        double sumCoin = dragonETH + fCoinETH;
        double sumMoney = dragonUSDT + fCoinUSDT;

        double avgMoney = sumMoney / 2;
        double avgCoin = sumCoin / 2;

        System.out.println("each money:" + avgMoney);
        System.out.println("each coin:" + avgCoin);

        System.out.println("to fcoin money:" + (dragonUSDT - avgMoney));
        System.out.println("to fcoin coin:" + (avgCoin - fCoinETH));

    }
}
