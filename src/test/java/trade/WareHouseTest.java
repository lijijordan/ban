package trade;


import com.jordan.ban.TradeApplication;
import com.jordan.ban.dao.WareHouseRepository;
import com.jordan.ban.domain.WareHouseState;
import com.jordan.ban.entity.WareHouse;
import com.jordan.ban.service.BackTestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeApplication.class)
@Slf4j
public class WareHouseTest {

    @Autowired
    private WareHouseRepository wareHouseRepository;

    @Autowired
    private BackTestService backTestService;


    @Test
    public void testSave() {
        this.wareHouseRepository.save(WareHouse.builder().volumeIn(0.1).state(WareHouseState.in)
                .timeIn(new Date()).diffPercentIn(0.0021).diffPercentOut(-0.06).build());
    }

    @Test
    public void testQuery() {
        List<WareHouse> list = this.wareHouseRepository.findAllByStateIsNot(WareHouseState.out);
        Assert.notEmpty(list);
        System.out.println(list);
    }

    @Test
    public void testSort() {
        List<WareHouse> wareHouses = this.wareHouseRepository.findAllByStateIsNot(WareHouseState.out);
        wareHouses.sort((o1, o2) -> {
            if (o1.getDiffPercentOut() >= o2.getDiffPercentOut()) {
                return 1;
            } else {
                return -1;
            }
        });
        wareHouses.forEach(wareHouse -> {
            System.out.println(wareHouse.getDiffPercentOut());
        });
    }

    @Test
    public void testCheckWareHouse() {
        double volume = this.backTestService.checkAndOutWareHouse(0.01, 0.28);
        System.out.println("volume:" + volume);
        org.junit.Assert.assertTrue(volume == 0.28);
    }
}
