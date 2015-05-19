package org.wzj.web;

import org.wzj.web.imp.RequestFacade;
import org.wzj.web.imp.ResponseFacade;

/**
 * Created by wens on 15-5-13.
 */
public class WebContext {

    private Request request;
    private Response response;


    public WebContext(Request request, Response response) {
        this.request = new RequestFacade(request);
        this.response = new ResponseFacade(response);
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }
}
