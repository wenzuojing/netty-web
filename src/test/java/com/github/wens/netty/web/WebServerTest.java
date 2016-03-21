package com.github.wens.netty.web;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;


public class WebServerTest extends TestCase {

    private static String URL_PRE = "http://127.0.0.1:9999";


    static {
        new Thread() {
            @Override
            public void run() {
                ServerConfig config = new ServerConfig();
                WebServer webServer = new WebServer(config);
                webServer.scanRouters("com.github.wens.netty.web.app");
                webServer.run();

            }
        }.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static HttpClient.Handler<Void> createBodyHandler(final String expectBody) {
        return new HttpClient.Handler<Void>() {
            @Override
            public Void handle(int statusCode, byte[] body, HttpURLConnection connection) {
                Assert.assertEquals(expectBody, new String(body));
                return null;
            }
        };
    }

    public void test_get_0() {
        HttpClient.get(URL_PRE + "/app/get_0").ok(createBodyHandler("get_0"));
    }

    public void test_get_1() {
        HttpClient.get(URL_PRE + "/app/get_1/777").ok(createBodyHandler("get_1"));
    }

    public void test_get_2() {
        HttpClient.get(URL_PRE + "/app/get_2/uuu-7776").ok(createBodyHandler("get_2"));
    }

    public void test_post_0() {
        HttpClient.post(URL_PRE + "/app/post_0").
                addFormItem("name", "zuojing").
                addFormItem("age", "30").
                addFormItem("score", "99.8").
                ok(createBodyHandler("post_0"));
    }

    public void test_post_1() {
        HttpClient.post(URL_PRE + "/app/post_1").
                addFormItem("name", "zuojing").
                addFormItem("age", "30").
                addFormItem("score", "99.8").
                ok(createBodyHandler("post_1"));
    }

    public void test_post_2() {
        HttpClient.post(URL_PRE + "/app/post_2/appkey").
                addFormItem("name", "zuojing").
                addFormItem("age", "30").
                addFormItem("score", "99.8").
                ok(createBodyHandler("post_2"));
    }

    public void test_post_3() {
        String playload = "{\"name\":\"zuojing\",\"age\":15}";
        HttpClient.post(URL_PRE + "/app/post_3").
                addPlayload(playload.getBytes()).
                ok(createBodyHandler(playload));
    }


    public void test_upload_file() {
        URL resource = WebServerTest.class.getResource("/META-INF/mime.types");
        HttpClient.post(URL_PRE + "/app/upload").
                addMultipartItem("myfile", new File(resource.getPath())).
                ok(createBodyHandler("upload ok"));

    }

}
