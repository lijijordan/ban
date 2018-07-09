package com.jordan.ban.market;

import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;

public class FeeUtils {

    public static double getFee(String market) {
        switch (market) {
            case Huobi.PLATFORM_NAME:
                return 0.002;
            case Fcoin.PLATFORM_NAME:
                return 0.001;
            case Dragonex.PLATFORM_NAME:
                return 0.002;
        }
        return 0.002;
    }

    public static void main(String[] args) {
        System.out.println(getFee(Fcoin.PLATFORM_NAME));
    }
}
