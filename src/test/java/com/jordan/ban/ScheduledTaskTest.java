package com.jordan.ban;


import com.jordan.ban.task.ScheduledTask;
import com.jordan.ban.task.StatisticTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackTestApplication.class)
@Slf4j
public class ScheduledTaskTest {

    @Autowired
    private ScheduledTask scheduledTask;

    @Autowired
    private StatisticTask statisticProfit;

    @Test
    public void testStatisticProfit(){
        statisticProfit.statisticDragonexVSHuobiProfit();
    }
}
