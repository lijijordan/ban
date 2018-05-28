package com.jordan.ban.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: liji
 * Date: 16/7/20
 * Time: 下午10:20
 * To change this template use File | Settings | File Templates.
 */
public class JSONUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Object to json string string.
     *
     * @param object the object
     * @return the string
     */
    public static String toJsonString(Object object) {
        String jsonString = "{}";
        try {
            jsonString = objectMapper.writeValueAsString(object);
            System.out.println("index json string:" + jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    /**
     * Gets entity.
     *
     * @param <T>        the type parameter
     * @param jsonString the json string
     * @param prototype  the prototype
     * @return the entity
     */
    public static <T> T getEntity(String jsonString, Class<T> prototype) {
        try {
            return objectMapper.readValue(jsonString, prototype);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
