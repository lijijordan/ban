package trade;


import com.jordan.ban.BackTestApplication;
import com.jordan.ban.dao.GridRepository;
import com.jordan.ban.entity.Grid;
import com.jordan.ban.service.BackTestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import static com.jordan.ban.common.Constant.ETH_USDT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackTestApplication.class)
@Slf4j
public class BackTestServiceTest {

    @Autowired
    private BackTestService backTestService;

    @Autowired
    private GridRepository gridRepository;

    @Test
    public void initAccount() {
    }

    @Test
    public void initGrid() {
        backTestService.initGrid(2,ETH_USDT);
        Assert.notNull(gridRepository.findAll());
    }

    @Test
    public void query() {
        Grid grid = gridRepository.find(0.021f, "ETHUSDT");
        Assert.notNull(grid);
        System.out.println(grid.getHigh());
    }

}
