package com.github.wens.netty.web;

/**
 * Created by wens on 15-5-15.
 */
public interface Response {

    void setStatus(int statusCode, String reasonPhrase);

    void setContentType(String contentType);

    void setContentLength(long contentLength);

    void writeBody(String body);

    void writeBody(byte[] body);

    void redirect(String location);

    void redirect(String location, int httpStatusCode);

    void setHeader(String header, String value);

    void sendError(int code, String reasonPhrase);


}
