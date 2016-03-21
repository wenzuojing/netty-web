package com.github.wens.netty.web;

import com.github.wens.netty.web.impl.DefaultObjectFactory;

/**
 * Created by wens on 15-5-13.
 */
public class ServerConfig {

    private String serverName = "netty-web-server";

    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;

    private int executorThreads = Runtime.getRuntime().availableProcessors() * 2;

    private String addr = "0.0.0.0";

    private int port = 9999;

    private String charset = "UTF-8";

    private String contextPath = "";

    private ObjectFactory objectFactory;

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

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getExecutorThreads() {
        return executorThreads;
    }

    public void setExecutorThreads(int executorThreads) {
        this.executorThreads = executorThreads;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory == null ? new DefaultObjectFactory() : objectFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}
