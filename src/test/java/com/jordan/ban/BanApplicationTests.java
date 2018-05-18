package com.jordan.ban;

import com.jordan.ban.domain.Differ;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.utils.JSONUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.UnknownHostException;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BanApplicationTests {

	@Test
	public void contextLoads() {
		System.out.println("Let us rock!");
	}

	@Test
	public void index() throws UnknownHostException {
		Differ differ = new Differ();
		differ.setCreateTime(new Date());
		differ.setDiffer(0.21f);
		differ.setPercentDiffer("21");
		differ.setSymbol("ETHBTT");
		ElasticSearchClient.initClient("localhost", 9300);
		ElasticSearchClient.index("differ_binance_otcbtc_1", "data", JSONUtil.toJsonString(differ));
	}
}
