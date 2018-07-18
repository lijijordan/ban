package service;


import com.jordan.ban.TradeApplication;
import com.jordan.ban.dao.OrderRepository;
import com.jordan.ban.dao.ProfitStatisticsRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.CycleType;
import com.jordan.ban.domain.StatisticRecordDto;
import com.jordan.ban.entity.Order;
import com.jordan.ban.entity.ProfitStatistics;
import com.jordan.ban.entity.TradeRecord;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeApplication.class)
@Slf4j
public class ProfitStatisticsTest {

    @Autowired
    private ProfitStatisticsRepository profitStatisticsRepository;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

    @Autowired
    private OrderRepository orderRepository;

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
    public void testFindRecord() {
        List<TradeRecord> list = tradeRecordRepository.findAllByCreateTime(new Date(-2l));
        Assert.assertNotNull(list);
        log.info(list.toString());
    }

    @Test
    public void testFindOrder() {
        List<Order> list = orderRepository.findAllByCreateTime(new Date(-1));
        Assert.assertNotNull(list);
        log.info(list.toString());
        System.out.println(new Date());
    }
    
}
