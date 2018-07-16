package service;


import com.jordan.ban.BackTestApplication;
import com.jordan.ban.dao.ProfitStatisticsRepository;
import com.jordan.ban.domain.CycleType;
import com.jordan.ban.domain.StatisticRecordDto;
import com.jordan.ban.entity.ProfitStatistics;
import com.jordan.ban.service.TradeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackTestApplication.class)
@Slf4j
public class ProfitStatisticsTest {

    @Autowired
    private ProfitStatisticsRepository profitStatisticsRepository;

    @Autowired
    private TradeRecordService tradeRecordService;

    @Test
    public void testSave() {
        profitStatisticsRepository.save(ProfitStatistics.builder().cycleType(CycleType.day).platformA("A").platformB("B").symbol("ltcusdt").sumMoney(100).sumCoin(10).build());
        profitStatisticsRepository.save(ProfitStatistics.builder().cycleType(CycleType.day).platformA("A").platformB("B").symbol("ltcusdt").sumMoney(110).sumCoin(10).build());
    }


    @Test
    public void testFind() {
        ProfitStatistics profitStatistics = profitStatisticsRepository.findTopBySymbolAndAndPlatformAAndAndPlatformBOrderByCreateTimeDesc("ltcusdt", "A", "B");
        System.out.println(profitStatistics.getSumMoney());
    }

    @Test
    public void testQuery(){
        StatisticRecordDto statisticRecordDto = tradeRecordService.queryAndStatisticTradeRecord();
        Assert.notNull(statisticRecordDto);
    }
}
