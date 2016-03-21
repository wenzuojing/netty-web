package com.github.wens.netty.web.example;

import com.github.wens.netty.web.HttpMethod;
import com.github.wens.netty.web.ServerConfig;
import com.github.wens.netty.web.WebServer;
import com.github.wens.netty.web.annotaction.Controller;
import com.github.wens.netty.web.annotaction.ParamValue;
import com.github.wens.netty.web.annotaction.Router;

/**
 * Created by wens on 15-9-1.
 */
@Controller
public class Helloworld2 {

    @Router(value = "/query", method = HttpMethod.ALL)
    public void hello1(@ParamValue("p1") String p1, @ParamValue("p2") String p2) {
        System.out.println(p1 + "," + p2);
    }


    public static void main(String[] args) {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(7777);
        WebServer webServer = new WebServer(serverConfig);
        webServer.scanRouters("com.github.wens.netty.web.example");
        webServer.run();
    }

}
