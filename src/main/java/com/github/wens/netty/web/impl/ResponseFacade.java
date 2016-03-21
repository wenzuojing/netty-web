package com.github.wens.netty.web.impl;

import com.github.wens.netty.web.Response;

/**
 * Created by wens on 15-5-19.
 */
public class ResponseFacade implements Response {

    private Response response;

    public ResponseFacade(Response response) {
        this.response = response;
    }

    @Override
    public void setStatus(int statusCode, String reasonPhrase) {
        response.setStatus(statusCode, reasonPhrase);
    }

    @Override
    public void setContentType(String contentType) {
        response.setContentType(contentType);
    }

    @Override
    public void setContentLength(long contentLength) {
        response.setContentLength(contentLength);
    }

    @Override
    public void writeBody(String body) {
        response.writeBody(body);
    }

    @Override
    public void writeBody(byte[] body) {
        response.writeBody(body);
    }

    @Override
    public void redirect(String location) {
        response.redirect(location);
    }

    @Override
    public void redirect(String location, int httpStatusCode) {
        response.redirect(location, httpStatusCode);
    }

    @Override
    public void setHeader(String header, String value) {
        response.setHeader(header, value);
    }

    @Override
    public void sendError(int code, String reasonPhrase) {
        response.sendError(code, reasonPhrase);
    }
}
