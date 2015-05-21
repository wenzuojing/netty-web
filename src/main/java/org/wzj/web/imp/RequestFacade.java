package org.wzj.web.imp;

import org.wzj.web.FileItem;
import org.wzj.web.Request;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wens on 15-5-19.
 */
public class RequestFacade implements Request {

    private Request request;

    public RequestFacade(Request request) {
        this.request = request;
    }

    @Override
    public Map<String, List<String>> getParams() {
        return request.getParams();
    }

    @Override
    public List<String> getParams(String name) {
        return request.getParams(name);
    }

    @Override
    public String getParam(String name) {
        return request.getParam(name);
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getUri() {
        return request.getUri();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    @Override
    public int getRemotePort() {
        return request.getRemotePort();
    }

    @Override
    public String getBodyAsString() {
        return request.getBodyAsString();
    }

    @Override
    public byte[] getBodyAsBytes() {
        return request.getBodyAsBytes();
    }

    @Override
    public String queryParams(String queryParam) {
        return request.queryParams(queryParam);
    }

    @Override
    public Set<String> queryParams() {
        return request.queryParams();
    }

    @Override
    public String getHeader(String header) {
        return request.getHeader(header);
    }

    @Override
    public Set<String> getHeaders() {
        return request.getHeaders();
    }

    @Override
    public FileItem getFile(String name) {
        return request.getFile(name);
    }

}
