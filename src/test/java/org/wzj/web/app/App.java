package org.wzj.web.app;

import org.wzj.web.FileItem;
import org.wzj.web.HttpMethod;
import org.wzj.web.WebContext;
import org.wzj.web.annotaction.*;

/**
 * Created by wens on 15-5-18.
 */
@Controller
public class App {


    @Router(value = "/app/get_0", method = HttpMethod.GET)
    public void get_0(WebContext context) {
        context.getResponse().writeBody("get_0");
    }

    @Router(value = "/app/get_1/{id:\\d{3}}", method = HttpMethod.GET)
    public void get_1(WebContext context, @PathValue("id") int id) {
        context.getResponse().writeBody("get_1");
    }

    @Router(value = "/app/get_2/{id:u{3}-\\d{3,6}}", method = HttpMethod.GET)
    public void get_2(WebContext context, @PathValue("id") String id) {
        if (id == null || id.length() == 0) {
            context.getResponse().writeBody("fail");
            return;
        }
        context.getResponse().writeBody("get_2");
    }

    @Router(value = "/app/post_0", method = HttpMethod.POST)
    public void post_0(WebContext context) {
        context.getResponse().writeBody("post_0");

    }

    @Router(value = "/app/post_1", method = HttpMethod.POST)
    public void post_1(WebContext context, @ParamValue("name") String name, @ParamValue("age") Integer age, @ParamValue("score") Double score) {
        if (name == null || age == null || score == null) {
            context.getResponse().writeBody("fail");
            return;
        }
        context.getResponse().writeBody("post_1");

    }

    @Router(value = "/app/post_2/{appkey}", method = HttpMethod.POST)
    public void post_2(WebContext context, @PathValue("appkey") String appkey, @ParamValue("name") String name, @ParamValue("age") Integer age, @ParamValue("score") Double score) {
        if (appkey == null || name == null || age == null || score == null) {
            context.getResponse().writeBody("fail");
            return;
        }
        context.getResponse().writeBody("post_2");
    }

    @Router(value = "/app/post_3", method = HttpMethod.POST)
    public void post_3(WebContext context, @BodyValue byte[] content) {
        if (content == null || content.length == 0) {
            context.getResponse().writeBody("fail");
            return;
        }

        context.getResponse().writeBody(new String(content));
    }

    @Router(value = "/app/upload", method = HttpMethod.POST)
    public void upload(WebContext context) {

        FileItem fileItem = context.getRequest().getFile("myfile");

        if (fileItem == null) {
            context.getResponse().writeBody("fail");
            return;
        }

        context.getResponse().writeBody("upload ok");

    }


}
