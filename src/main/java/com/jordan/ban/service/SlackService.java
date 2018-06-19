package com.jordan.ban.service;

import com.jordan.ban.http.HttpClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Slf4j
@Service
public class SlackService {

    public static final String WEB_HOOK_URL = "https://hooks.slack.com/services/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";
    public static final String PAYLOAD_TPL = "{\"channel@liji.jordan\": \"#general\", \"username\": \"%s\", \"text\": \"%s.\", \"icon_emoji\": \":moneybag:\"}";


    /**
     * curl -X POST --data-urlencode "payload={\"channel@liji.jordan\": \"#general\", \"username\": \"webhookbot\",
     * \"text\": \"Hello LJ let us rock it.\", \"icon_emoji\": \":moneybag:\"}" https://hooks.slack.com/services/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
     *
     * @param title
     * @param content
     */
    @Async
    public void sendMessage(String title, String content) throws IOException {
        HttpPost post = new HttpPost(WEB_HOOK_URL);
        post.setEntity(new StringEntity(String.format(PAYLOAD_TPL, title, content)));
        CloseableHttpResponse response;
        response = (CloseableHttpResponse) HttpClientFactory.getHttpClient().execute(post);
        HttpEntity entity = response.getEntity();
        try {
            System.out.println(EntityUtils.toString(entity, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        SlackService slackService = new SlackService();
        slackService.sendMessage("Ban", "Data:MockTradeResultIndex(eatDiff=-0.1299999999999949, eatPercent=-0.001333333333333281, tradeDiff=-0.05028000000001076, tradePercent=-5.15322332684337E-4, tradeDirect=B2A, createTime=Tue Jun 19 12:15:46 CST 2018, costTime=1193, symbol=LTCUSDT, diffPlatform=Huobi-Fcoin, eatTradeVolume=0.3734, sellCost=36.333687000000005, buyCost=36.236797167999995, tradeVolume=30.5553, sellPrice=97.5, buyPrice=97.24)");
    }
}
