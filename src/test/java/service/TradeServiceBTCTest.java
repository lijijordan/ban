package service;


import com.jordan.ban.TradeApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeApplication.class)
@Slf4j
public class TradeServiceBTCTest {

    @Autowired
//    private TradeServiceBTC tradeService;


    @Test
    public void testTrade() {
        String jsonB2A = "{\n" +
                "  \"tradeVolume\": 0.2331,\n" +
                "  \"buyPrice\": 7025.54,\n" +
                "  \"symbol\": \"BTCUSDT\",\n" +
                "  \"buyCost\": 14.724126731999998,\n" +
                "  \"eatDiff\": -5.539999999999964,\n" +
                "  \"sellCost\": 14.712515999999999,\n" +
                "  \"diffPlatform\": \"Dragonex-Fcoin\",\n" +
                "  \"eatTradeVolume\": 0.0021,\n" +
                "  \"sellPrice\": 7020,\n" +
                "  \"tradeDiff\": -20.155157999999737,\n" +
                "  \"createTime\": 1533627960848,\n" +
                "  \"costTime\": 626,\n" +
                "  \"tradeDirect\": \"B2A\",\n" +
                "  \"eatPercent\": 0.00586,\n" +
                "  \"tradePercent\": -0.002866036693371777\n" +
                "}";

        String jsonA2B = "{\n" +
                "  \"tradeVolume\": 0.0021,\n" +
                "  \"buyPrice\": 7032.4145,\n" +
                "  \"symbol\": \"BTCUSDT\",\n" +
                "  \"buyCost\": 1642.5343315899,\n" +
                "  \"eatDiff\": -7.974500000000262,\n" +
                "  \"sellCost\": 1634.122170072,\n" +
                "  \"diffPlatform\": \"Dragonex-Fcoin\",\n" +
                "  \"eatTradeVolume\": 0.2331,\n" +
                "  \"sellPrice\": 7024.44,\n" +
                "  \"tradeDiff\": -22.562160000000038,\n" +
                "  \"createTime\": 1533627960848,\n" +
                "  \"costTime\": 626,\n" +
                "  \"tradeDirect\": \"A2B\",\n" +
                "  \"eatPercent\": 0.001133963306628223,\n" +
                "  \"tradePercent\": -0.0032114485149896004\n" +
                "}";


//        this.tradeService.trade(JSONUtil.getEntity(jsonB2A, MockTradeResultIndex.class));
    }
}
