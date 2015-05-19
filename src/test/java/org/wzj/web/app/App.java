package org.wzj.web.app;

import org.wzj.web.HttpMethod;
import org.wzj.web.WebContext;
import org.wzj.web.annotaction.Controller;
import org.wzj.web.annotaction.Router;

import java.util.List;
import java.util.Map;

/**
 * Created by wens on 15-5-18.
 */
@Controller
public class App {


    @Router(value = "/app/get", method = HttpMethod.GET)
    public void get(WebContext context) {

        System.out.println(context.getRequest().getRemoteAddr());
        context.getResponse().writeBody("hello world!");
        System.out.println("------------get---------");

    }

    @Router(value = "/app/post", method = HttpMethod.POST)
    public void post(WebContext context) {
        System.out.println("-----------post---------");
        Map<String, List<String>> params = context.getRequest().getParams();

        for (String name : params.keySet()) {
            System.out.println(name);
            String tab = " ";

            for (String val : params.get(name)) {
                System.out.println(tab + val);
            }
        }

    }


}
