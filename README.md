# netty-web

netty-web在netty4的基础上做了轻量级封装及增强，提供方便快捷开发web应用，特别适合用来开发api类应用。

## features

* 支持路由
* 支持rest风格url
* 支持表单参数注入
* 支持文件上传
* 支持Spring集成

## hello world

```java
@Controller
public class Helloworld {


    @Router("/hello1/{name}")
    public void hello1(WebContext context, @PathValue("name") String name) {
        context.getResponse().writeBody("Hello," + name + "\r\n");
    }

    @Router("/hello2")
    public void hello2(WebContext context, @ParamValue("name") String name) {
        context.getResponse().writeBody("Hello," + name + "\r\n");
    }

    @Router(value = "/hello3", method = HttpMethod.POST)
    public void hello3(WebContext context, @BodyValue byte[] body) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> info = objectMapper.readValue(body, new TypeReference<Map<String, String>>() {
        });
        context.getResponse().writeBody("Hello," + info.get("name") + "\r\n");
    }

    //返回值可以任意类型，netty-web默认把放回值序列化json放回给客户端
    @Router("/hello4/{name}")
    public Result hello4(@PathValue("name") String name) {
        return new Result(200, "Hello," + name);
    }

    public static void main(String[] args) {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(7777);
        WebServer webServer = new WebServer(serverConfig);
        webServer.scanRouters("com.github.wens.netty.web.example");
        webServer.run();
    }

}

class Result {

    private int code;
    private String msg;

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

```

1. hello1
    ```
    curl http://localhost:7777/hello1/wens
    ```
    返回结果
    ```
    Hello,wens
    ```
2. hello2
    ```
    curl http://localhost:7777/hello2?name=wens
    ```
    返回结果
    ```
    Hello,wens
    ```
3. hello3
    ```
    curl -XPOST  http://localhost:7777/hello3 -d '{"name" : "wens"}'
    ```
    返回结果
    ```
    Hello,wens
    ```
3. hello4
    ```
    curl http://localhost:7777/hello4/wens
    ```
    返回结果
    ```
    {"code":200,"msg":"Hello,wens"}
    ```

## 支持的注解

* @Controller 
    Controller类不需要继续特定接口，只需要使用@Controller注解就可以了
* @Router
    注册路由信息，其中method属性标记http请求方法，默认的情况下为HttpMethod.GET，注册为通用处理方法可以使用HttpMethod.ALL,相当于spring mvc @RequestMapping
* @PathValue
    用于处理方法参数注入，@PathValue是用来获得请求url中的动态参数,相当于spring mvc @PathVariable
* @ParamValue
    用于处理方法参数注入，@ParamValue是用来获得请求参数,相当于spring mvc @RequestParam
* @BodyValue
    用于处理方法参数注入，@BodyValue是用来获得请求playload
    
## 路由
* 非正则路由
    "/api/index",只能匹配/api/index请求url
* 正则路由
    "/api/{appkey:[A-Z]+}/videos/{pagesize:\d+}",其中appkey必须为大写字母&pagesize为数字，/api/ABC/videos/10能匹配上，/api/abc/videos/10则不能




  
