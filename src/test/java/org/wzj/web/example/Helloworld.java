package org.wzj.web.example;

import org.wzj.web.Web;
import org.wzj.web.WebContext;
import org.wzj.web.annotaction.Controller;
import org.wzj.web.annotaction.PathValue;
import org.wzj.web.annotaction.Router;

/**
 * Created by wens on 15-5-21.
 */
@Controller
public class Helloworld {

    @Router("/hi/{name}")
    public void hi(WebContext context, @PathValue("name") String name) {
        context.getResponse().writeBody("Hi," + name + "\r\n");
    }

    public static void main(String[] args) {
        Web.scanRouters("org.wzj.web.example");
        Web.run();
    }

}
