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
public class BanApplicationTests {

    @Test
    public void contextLoads() {
        System.out.println("Let us rock!");
    }


    @Test
    public void compare() {
        int timeout = 2;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000).
                setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
        CloseableHttpClient closeableHttpClient = HttpClients.custom().setDefaultRequestConfig(config).build();

        System.out.println("Let us rock!");
        new Thread(() -> {
            long max = 0, min = 10000, avg, current, sum = 0;
            int i = 0;
            while (true) {
                i++;
                try {
                    long start = System.currentTimeMillis();
                    CompareApplication.comparePrice(CompareApplication.parseBittrexTickers(), CompareApplication.parseBinanceTickers());
                    long end = System.currentTimeMillis();
                    current = end - start;
                    if (max < current) {
                        max = current;
                    }
                    if (min > current) {
                        min = current;
                    }
                    sum = sum + current;
                    avg = sum / i;
                    System.out.println(String.format("Compare method cost createTime:【%s】ms, " +
                            "avg:【%s】ms, max:【%s】ms, min:【%s】ms , createTime: %s", current, avg, max, min, new Date()));
                } catch (Exception e) {
                    try {
                        closeableHttpClient.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();

        while (true) {
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("========================= tick ===========================");
        }
    }

	/*@Test
    public void index() throws UnknownHostException {
		Differ differ = new Differ();
		differ.setCreateTime(new Date());
		differ.setDiffer(0.21f);
		differ.setPercentDiffer("21");
		differ.setSymbol("ETHBTT");
		ElasticSearchClient.initClient("localhost", 9300);
		ElasticSearchClient.index("differ_binance_otcbtc_1", "data", JSONUtil.toJsonString(differ));
	}*/
}
