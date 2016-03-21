package com.github.wens.netty.web.util;

import com.github.wens.netty.web.WebException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 简单的json处理工具
 *
 * @todolist 重构抽象成接口
 * <p/>
 * Created by wens on 15-9-2.
 */
public class JsonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        try {
            objectMapper.writeValue(out, obj);
            return out.toByteArray();
        } catch (IOException e) {
            throw new WebException("Serialize json fail.", e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                //ignore
            }
        }


    }


}
