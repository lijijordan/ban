package com.jordan.ban;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ELKTest {
    public static void main(String[] args) throws UnknownHostException {
        // on startup
        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

// on shutdown

        String json = "{\"createTime\":1526728981448}";

        IndexResponse response = client.prepareIndex("test_1", "data")
                .setSource(json, XContentType.JSON)
                .get();
        System.out.println(response);

        client.close();
    }
}
