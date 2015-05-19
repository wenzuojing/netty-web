package org.wzj.web;

import junit.framework.TestCase;


public class WebServerTest extends TestCase {


    @Override
    protected void setUp() throws Exception {
        new Thread() {
            @Override
            public void run() {

                ServerConfig config = new ServerConfig();
                config.setStaticDir("/home/wens/share");
                WebServer webServer = new WebServer();
                webServer.staticFile("/public/.*");
                webServer.scanRouters("org.wzj.web.app");
                webServer.run(config);

            }
        }.start();
    }


    public void test_staticFile() throws InterruptedException {

        //Runtime.getRuntime().ex

        Thread.sleep(1000000);

    }


}
