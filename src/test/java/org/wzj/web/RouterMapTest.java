package org.wzj.web;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by wens on 15-5-14.
 */
public class RouterMapTest {


    @Test
    public void test_0() throws RuntimeException {

        RouterMap routerMap = new RouterMap();

        routerMap.addRouter("/api/index", "GET", "org.wzj.web.app.VideoController.index");
        routerMap.addRouter("/api/index", "GET", "org.wzj.web.app.VideoController.index()");
        routerMap.addRouter("/api/.*/like/{id}", "GET", "org.wzj.web.app.VideoController.like");
        routerMap.addRouter("/api/{appkey}/videos", "GET", "org.wzj.web.app.VideoController.videos(java.lang.String)");
        routerMap.addRouter("/api/{appkey}/video/{videoid}", "GET", "org.wzj.web.app.VideoController.video");
        routerMap.addRouter("/api/{appkey:[A-Z]+}/videos/{pagesize:\\d+}", "GET", "org.wzj.web.app.VideoController.videos(java.lang.String, java.lang.String)");

        Assert.assertEquals(true, routerMap.handle("GET", "/api/index", null));
        Assert.assertEquals(false, routerMap.handle("POST", "/api/index", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/inde", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/index2", null));
        Assert.assertEquals(true, routerMap.handle("GET", "/api/dd/d/like/11", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/dd/d/like/11/55", null));
        Assert.assertEquals(true, routerMap.handle("GET", "/api/appkey01/videos", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/appkey01/d/videos", null));
        Assert.assertEquals(true, routerMap.handle("GET", "/api/appkey01/video/88fdfdf", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/appkey01/d/video/88fdfdf", null));
        Assert.assertEquals(true, routerMap.handle("GET", "/api/APPKEY/videos/2222", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/appkey/videos/2222", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/APPKEY/videos/2222id", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/APPKEY/videos/2222/id", null));

    }


    @Test
    public void test_1() {

        RouterMap routerMap = new RouterMap();

        routerMap.scanRouters("org.wzj.web.app");


        Assert.assertEquals(true, routerMap.handle("GET", "/api/index", null));
        Assert.assertEquals(false, routerMap.handle("POST", "/api/index", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/inde", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/index2", null));
        Assert.assertEquals(true, routerMap.handle("GET", "/api/dd/d/like/11", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/dd/d/like/11/55", null));
        Assert.assertEquals(true, routerMap.handle("GET", "/api/appkey01/videos", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/appkey01/d/videos", null));
        Assert.assertEquals(true, routerMap.handle("GET", "/api/appkey01/video/88fdfdf", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/appkey01/d/video/88fdfdf", null));
        Assert.assertEquals(true, routerMap.handle("GET", "/api/APPKEY/videos/2222", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/appkey/videos/2222", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/APPKEY/videos/2222id", null));
        Assert.assertEquals(false, routerMap.handle("GET", "/api/APPKEY/videos/2222/id", null));

    }


}
