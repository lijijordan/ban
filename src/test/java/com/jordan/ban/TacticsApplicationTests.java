package com.jordan.ban;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Date;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TacticsApplicationTests {


    public void contextLoads() {
        System.out.println("Let us rock!");
    }





	/*@Test
    public void index() throws UnknownHostException {
		Differ policy = new Differ();
		policy.setCreateTime(new Date());
		policy.setDiffer(0.21f);
		policy.setPercentDiffer("21");
		policy.setSymbol("ETHBTT");
		ElasticSearchClient.initClient("localhost", 9300);
		ElasticSearchClient.index("differ_binance_otcbtc_1", "data", JSONUtil.toJsonString(policy));
	}*/
}
