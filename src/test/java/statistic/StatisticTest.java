package statistic;


import com.jordan.ban.TradeApplication;
import com.jordan.ban.service.StatisticService;
import com.jordan.ban.task.StatisticTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeApplication.class)
@Slf4j
public class StatisticTest {

    @Autowired
    private StatisticTask statisticTask;

    @Autowired
    StatisticService statisticService;

    @Test
    public void testStatistic() {
        statisticTask.statisticDragonexVSHuobiProfit();
    }


    @Test
    public void Test_Count_order() {
        statisticService.singleGridStatistic();
    }
}
