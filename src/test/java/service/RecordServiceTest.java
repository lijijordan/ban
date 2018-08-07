package service;


import com.jordan.ban.TradeApplication;
import com.jordan.ban.domain.StatisticRecordDto;
import com.jordan.ban.service.TradeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeApplication.class)
@Slf4j
public class RecordServiceTest {

    @Autowired
    private TradeRecordService tradeRecordService;

    @Test
    public void testQueryAndStasitic(){
        StatisticRecordDto statisticRecordDto = this.tradeRecordService.queryAndStatisticTradeRecord(new Date(-1));
        Assert.notNull(statisticRecordDto);
        System.out.println(statisticRecordDto);
    }
}
