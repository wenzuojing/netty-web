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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wzj.web.FileItem;
import org.wzj.web.PostFormDataDecoder;
import org.wzj.web.Request;
import org.wzj.web.WebException;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wens on 15-5-15.
 */
public class RequestImp implements Request {

    private static final Logger log = LoggerFactory.getLogger(RequestImp.class);

    private ChannelHandlerContext ctx;
    private FullHttpRequest httpRequest;


    private Map<String, List<String>> queryStringParams;

    private Map<String, List<String>> params;
    private Map<String, FileItem> files;

    private byte[] bodyBytes = null;
    private String bodyString = null;


    public RequestImp(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
        this.ctx = ctx;
        this.httpRequest = httpRequest;
        parseParams();
        readBody();
    }

    @Override
    public Map<String, List<String>> getParams() {
        return Collections.unmodifiableMap(params);
    }

    @Override
    public List<String> getParams(String name) {
        return params.get(name);
    }

    @Override
    public String getParam(String name) {
        List<String> values = params.get(name);
        return values == null || values.size() == 0 ? null : values.get(0);
    }

    @Override
    public FileItem getFile(String name) {
        return files.get(name);
    }

    private void parseParams() {
        PostFormDataDecoder postFormDataDecoder = null;
        try {
            postFormDataDecoder = new PostFormDataDecoder(httpRequest);
            params = postFormDataDecoder.getParams();
            files = postFormDataDecoder.getFiles();
        } catch (Exception e) {
            throw new WebException("Exception when decode post form data.", e);
        } finally {
            if (postFormDataDecoder != null) {
                postFormDataDecoder.release();
            }
        }
    }

    @Override
    public String getMethod() {
        return httpRequest.getMethod().name();
    }


    @Override
    public String getUri() {
        return httpRequest.getUri();
    }


    @Override
    public String getContentType() {
        return httpRequest.headers().get(HttpHeaders.Names.CONTENT_TYPE);
    }


    @Override
    public String getRemoteAddr() {
        SocketAddress socketAddress = ctx.channel().remoteAddress();

        if (socketAddress instanceof InetSocketAddress) {
            return ((InetSocketAddress) socketAddress).getHostName();
        }

        return "0.0.0.0";
    }

    @Override
    public int getRemotePort() {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        if (socketAddress instanceof InetSocketAddress) {
            return ((InetSocketAddress) socketAddress).getPort();
        }
        return 0;
    }


    @Override
    public String getBodyAsString() {

        return bodyString;
    }

    @Override
    public byte[] getBodyAsBytes() {

        return bodyBytes;
    }

    private void readBody() {
        try {
            ByteBuf content = httpRequest.content();
            int readableBytes = content.readableBytes();
            bodyBytes = new byte[readableBytes];
            content.readBytes(bodyBytes);
            bodyString = new String(bodyBytes);

        } catch (Exception e) {
            throw new WebException("Exception when reading body", e);
        }

    }

    @Override
    public String queryParams(String queryParam) {
        if (queryStringParams == null) {
            readQueryString();
        }
        List<String> values = queryStringParams.get(queryParam);
        return values == null || values.size() == 0 ? null : values.get(0);
    }

    private void readQueryString() {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(httpRequest.getUri());
        queryStringParams = queryStringDecoder.parameters();
    }

    @Override
    public Set<String> queryParams() {
        if (queryStringParams == null) {
            readQueryString();
        }
        return queryStringParams.keySet();
    }


    @Override
    public String getHeader(String header) {
        return httpRequest.headers().get(header);
    }


    public Set<String> getHeaders() {
        return httpRequest.headers().names();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder(100);

        sb.append("[request] ").append(getMethod()).append(" ").append(getUri()).append(" ").append(getRemoteAddr()).append(":").append(getRemotePort());

        sb.append(" header{");

        for (String header : getHeaders()) {
            sb.append(header).append("=").append(getHeader(header)).append(",");
        }

        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("}");

        return sb.toString();
    }
}
