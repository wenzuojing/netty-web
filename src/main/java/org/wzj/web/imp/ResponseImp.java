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

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wzj.web.Response;
import org.wzj.web.WebException;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;


/**
 * Created by wens on 15-5-15.
 */
public class ResponseImp implements Response {

    private static final Logger log = LoggerFactory.getLogger(ResponseImp.class);

    private ChannelHandlerContext ctx;
    private FullHttpResponse response;

    private volatile boolean finish = false;

    private volatile long contentLength = -1;


    public ResponseImp(ChannelHandlerContext ctx, FullHttpResponse response) {
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
        this.contentLength = contentLength;
    }


    @Override
    public void writeBody(String body) {
        checkStatus();
        response.content().writeBytes(body.getBytes());
    }

    @Override
    public void writeBody(byte[] body) {
        checkStatus();
        response.content().writeBytes(body);
    }

    @Override
    public void writeFile(File file)  {
        checkStatus();

        FileInputStream fileInputStream = null ;
        try {
            fileInputStream = new FileInputStream(file) ;
        } catch (FileNotFoundException e) {
            throw new WebException("File not found." , e ) ;
        }


        byte[] buf = new byte[1024] ;
        BufferedInputStream bufInput = null ;
        try {
            bufInput = new BufferedInputStream( fileInputStream , 1024) ;
            while (true){
                int reads = bufInput.read(buf) ;
                if(reads == -1 ){
                    break;
                }
                response.content().writeBytes(buf,0 ,reads ) ;
            }
        } catch (IOException e) {
            throw new WebException("Read file fail.", e);
        }finally {

            if(bufInput != null ){
                try {
                    bufInput.close();
                } catch (IOException e) {
                    //
                }
            }

            if(fileInputStream != null ){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    //
                }
            }

        }
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
        finish(false);
    }

    public void finish(boolean keepAlive) {
        finish = true;
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, contentLength == -1 ? response.content().readableBytes() : contentLength);
        if (!keepAlive) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            ctx.writeAndFlush(response);
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
