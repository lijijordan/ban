package service;


import com.jordan.ban.BackTestApplication;
import com.jordan.ban.entity.Platform;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.service.PlatformService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackTestApplication.class)
@Slf4j
public class PlatformServiceTest {


    @Autowired
    private PlatformService platformService;

    @Test
    public void testCreatePlatform() {
        String name = Huobi.PLATFORM_NAME;
        Platform platform = this.platformService.createPlatform(name);
        Assert.assertNotNull(platform);
    }
}
