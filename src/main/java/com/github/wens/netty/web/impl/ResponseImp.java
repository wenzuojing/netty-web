/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.wens.netty.web.impl;

import com.github.wens.netty.web.Response;
import com.github.wens.netty.web.WebException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;


/**
 * Created by wens on 15-5-15.
 */
public class ResponseImp implements Response {

    private static final Logger log = LoggerFactory.getLogger(ResponseImp.class);

    private String charset;

    private ChannelHandlerContext ctx;
    private HttpResponse response;
    private ByteBuf byteBuf;

    private volatile boolean finish = false;
    private long contentLength = 0;

    public ResponseImp(String charset, ChannelHandlerContext ctx, HttpResponse response) {
        this.charset = charset;
        this.ctx = ctx;
        this.response = response;
        this.byteBuf = ctx.alloc().buffer(100);
    }

    @Override
    public void setStatus(int statusCode, String reasonPhrase) {
        response.setStatus(new HttpResponseStatus(statusCode, reasonPhrase));
    }

    @Override
    public void setContentType(String contentType) {
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);
    }

    @Override
    public void setContentLength(long contentLength) {
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, contentLength);
    }


    @Override
    public void writeBody(String body) {
        if (body == null) {
            return;
        }
        try {
            writeBody(body.getBytes(this.charset));
        } catch (UnsupportedEncodingException e) {
            //
        }
    }

    @Override
    public void writeBody(byte[] body) {
        checkStatus();
        if (body == null || body.length == 0) {
            return;
        }
        contentLength += body.length;
        byteBuf.writeBytes(body);
    }

    private void writeHeader(boolean keepAlive) {
        if (!response.headers().contains(HttpHeaders.Names.CONTENT_LENGTH)) {
            setContentLength(contentLength);
        }
        if (keepAlive) {
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        ctx.write(response);
    }

    @Override
    public void redirect(String location) {
        redirect(location, 301);
    }


    @Override
    public void redirect(String location, int httpStatusCode) {
        checkStatus();
        if (log.isDebugEnabled()) {
            log.debug("Redirecting ({} to {}", httpStatusCode, location);
        }
        setStatus(httpStatusCode, "redirect");
        setHeader(HttpHeaders.Names.LOCATION, location);
        setHeader(HttpHeaders.Names.CONNECTION, "close");
        finish(false);

    }

    @Override
    public void setHeader(String header, String value) {
        response.headers().set(header, value);
    }

    @Override
    public void sendError(int code, String reasonPhrase) {
        setContentType(String.format("text/plain; charset=%s", charset));
        setStatus(code, reasonPhrase);
        setHeader(HttpHeaders.Names.CONNECTION, "close");
        finish(false);
    }

    public synchronized void finish(boolean keepAlive) {
        finish = true;
        writeHeader(keepAlive);
        writeBody();
        if (!keepAlive) {
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
        } else {

            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        }
    }

    private void writeBody() {
        this.ctx.write(this.byteBuf);
    }


    public boolean hasFinish() {
        return finish;
    }

    public void checkStatus() {
        if (finish) {
            throw new WebException("Response is finish.");
        }
    }

    @Override
    public String toString() {
        return "[response] " + response.getStatus();
    }


}
