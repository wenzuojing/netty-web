package org.wzj.web;

/**
 * Created by wens on 15-5-13.
 */
public class ServerConfig {

    private String serverName = "netty-web-server";

    private int maxConns = 200;

    private String staticDir;

    private String addr = "0.0.0.0";

    private int port = 9999;

    public String getStaticDir() {
        return staticDir;
    }

    public void setStaticDir(String staticDir) {
        this.staticDir = staticDir;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getMaxConns() {
        return maxConns;
    }

    public void setMaxConns(int maxConns) {
        this.maxConns = maxConns;
    }
}
