# netty-web

netty-web在netty4的基础上做了轻量级封装及增强，提供方便快捷开发web应用，特别适合用来开发api类应用。

## features

* 支持路由
* 支持rest风格url
* 支持表单参数注入
* 支持高效静态文件访问
* 支持文件上传

todo list

* http2、websocket
* 拦截器

## example

```java
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
```
执行上面的代码后，打开浏览器访问[http://localhost:9999/hi/zuojing](http://localhost:9999/hi/zuojing)




