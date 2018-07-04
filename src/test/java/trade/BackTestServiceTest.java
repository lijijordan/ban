package trade;


import com.jordan.ban.BackTestApplication;
import com.jordan.ban.service.BackTestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackTestApplication.class)
@Slf4j
public class BackTestServiceTest {

    @Autowired
    private BackTestService backTestService;

    @Test
    public void initAccount(){
        this.backTestService.init(498.2700	);
    }
}
