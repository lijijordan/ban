package service;


import com.jordan.ban.TacticsApplication;
import com.jordan.ban.dao.ProfitStatisticsRepository;
import com.jordan.ban.domain.CycleType;
import com.jordan.ban.entity.ProfitStatistics;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TacticsApplication.class)
@Slf4j
public class ProfitStatisticsTest {

    @Autowired
    private ProfitStatisticsRepository profitStatisticsRepository;

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
}
