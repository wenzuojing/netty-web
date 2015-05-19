package org.wzj.web;

/**
 * Created by wens on 15-5-13.
 */
public class Web {

    private static WebServer webServer = new WebServer();

    public static void staticFile(String route) {
        webServer.staticFile(route);
    }

    public static void get(String route, String handle) {
        webServer.get(route, handle);
    }

    public static void post(String route, String handle) {
        webServer.post(route, handle);
    }

    public static void put(String route, String handle) {
        webServer.put(route, handle);
    }

    public static void delete(String route, String handle) {
        webServer.delete(route, handle);
    }

    public static void match(String route, String handle) {
        webServer.get(route, handle);
    }

    public static void scanRouters(String packageName) {
        webServer.scanRouters(packageName);
    }

    public static void run() {
        run(null);
    }

    public static void run(String addr, int port) {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setAddr(addr);
        serverConfig.setPort(port);
        run(serverConfig);
    }

    public static void run(ServerConfig serverConfig) {
        webServer.run(serverConfig);
    }


}
