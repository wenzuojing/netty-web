package com.github.wens.netty.web.example;

/**
 * Created by wens on 15-9-2.
 */
public class Result {

    private int code;
    private String msg;

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
