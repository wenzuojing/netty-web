package org.wzj.web.example;

import org.wzj.web.ServerConfig;
import org.wzj.web.Web;
import org.wzj.web.WebContext;
import org.wzj.web.annotaction.Controller;
import org.wzj.web.annotaction.ParamValue;
import org.wzj.web.annotaction.Router;

/**
 * Created by wens on 15-5-21.
 */
@Controller
public class Helloworld {


    static String content = new String();

    static {

        for (int i = 0; i < 1024; i++) {
            content += "A";
        }

    }


    @Router("/test")
    public void test(WebContext context, @ParamValue("name") String name) {
        context.getResponse().writeBody(content);
        /*try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/


    }

    public static void main(String[] args) {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setMaxConns(100);
        Web.scanRouters("org.wzj.web.example");
        Web.run(serverConfig);
    }

}
