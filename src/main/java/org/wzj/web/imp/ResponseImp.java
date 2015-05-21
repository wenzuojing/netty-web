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
package org.wzj.web.imp;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wzj.web.Response;
import org.wzj.web.WebException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;


/**
 * Created by wens on 15-5-15.
 */
public class ResponseImp implements Response {

    private static final Logger log = LoggerFactory.getLogger(ResponseImp.class);

    private ChannelHandlerContext ctx;
    private HttpResponse response;

    private boolean finish = false;
    private boolean writeHeader = false;
    private boolean hasBodyData = false;

    public ResponseImp(ChannelHandlerContext ctx, HttpResponse response) {
        this.ctx = ctx;
        this.response = response;
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
        writeBody0(Unpooled.wrappedBuffer(body.getBytes()));
    }

    @Override
    public void writeBody(byte[] body) {
        writeBody0(Unpooled.wrappedBuffer(body));
    }

    @Override
    public void writeFile(File file) {
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            throw new WebException("File not found.", e);
        }

        long fileLength = file.length();

        writeBody0(new DefaultFileRegion(raf.getChannel(), 0, fileLength));

    }

    private synchronized void writeBody0(Object body) {
        checkStatus();
        hasBodyData = true;
        if (!writeHeader) {
            writeHeader();
        }
        ctx.write(body);
    }

    private void writeHeader() {
        writeHeader = true;
        if (!response.headers().contains(HttpHeaders.Names.CONTENT_LENGTH)) {
            if (hasBodyData) {
                response.headers().set(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
            } else {
                setContentLength(0);
            }
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
        setContentType("text/plain; charset=UTF-8");
        setStatus(code, reasonPhrase);
        setHeader(HttpHeaders.Names.CONNECTION, "close");
        finish(false);
    }

    public synchronized void finish(boolean keepAlive) {

        if (!writeHeader) {
            writeHeader();
        }

        finish = true;
        if (!keepAlive) {
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        }
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
