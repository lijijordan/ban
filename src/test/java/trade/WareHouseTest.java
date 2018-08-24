package trade;


import com.jordan.ban.TradeApplication;
import com.jordan.ban.dao.WareHouseRepository;
import com.jordan.ban.domain.WareHouseState;
import com.jordan.ban.entity.WareHouse;
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

    @Test
    public void testSave() {
        this.wareHouseRepository.save(WareHouse.builder().volumeIn(0.1).state(WareHouseState.in)
                .timeIn(new Date()).diffPercentIn(0.0021).diffPercentOut(-0.06).build());
    }

}