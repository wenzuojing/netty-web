package com.github.wens.netty.web.example;

import com.github.wens.netty.web.HttpMethod;
import com.github.wens.netty.web.ServerConfig;
import com.github.wens.netty.web.WebContext;
import com.github.wens.netty.web.WebServer;
import com.github.wens.netty.web.annotaction.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.Map;

/**
 * Created by wens on 15-9-1.
 */
@Controller
public class Helloworld {

    @Router("/hello1/{name}")
    public void hello1(WebContext context, @PathValue("name") String name) {
        context.getResponse().writeBody("Hello," + name + "\r\n");
    }

    @Router("/hello2")
    public void hello2(WebContext context, @ParamValue("name") String name) {
        context.getResponse().writeBody("Hello," + name + "\r\n");
    }

    @Router(value = "/hello3", method = HttpMethod.POST)
    public void hello3(WebContext context, @BodyValue byte[] body) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> info = objectMapper.readValue(body, new TypeReference<Map<String, String>>() {
        });
        context.getResponse().writeBody("Hello," + info.get("name") + "\r\n");
    }

    @Router("/hello4/{name}")
    public Result hello4(@PathValue("name") String name) {
        return new Result(200, "Hello," + name);
    }

    public static void main(String[] args) {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(7777);
        WebServer webServer = new WebServer(serverConfig);
        webServer.scanRouters("com.github.wens.netty.web.example");
        webServer.run();
    }

}
