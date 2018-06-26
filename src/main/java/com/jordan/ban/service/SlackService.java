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
    public void sendMessage(String title, String content) {
        HttpPost post = new HttpPost(WEB_HOOK_URL);
        try {
            post.setEntity(new StringEntity(String.format(PAYLOAD_TPL, title, content)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        CloseableHttpResponse response;
        try {
            response = (CloseableHttpResponse) HttpClientFactory.getHttpClient().execute(post);
            HttpEntity entity = response.getEntity();
            System.out.println(EntityUtils.toString(entity, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        SlackService slackService = new SlackService();
        slackService.sendMessage("Ban", "test");
    }
}
