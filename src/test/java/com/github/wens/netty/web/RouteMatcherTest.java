package com.github.wens.netty.web;

import com.github.wens.netty.web.route.RouteMatcher;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by wens on 15-9-1.
 */
public class RouteMatcherTest {

    @Test
    public void test_0() throws RuntimeException {

        RouteMatcher routeMatch = new RouteMatcher("");

        routeMatch.addRouter("/api/index", "GET", "com.github.wens.netty.web.app.VideoController.index");
        routeMatch.addRouter("/api/index", "GET", "com.github.wens.netty.web.app.VideoController.index()");
        routeMatch.addRouter("/api/.*/like/{id}", "GET", "com.github.wens.netty.web.app.VideoController.like");
        routeMatch.addRouter("/api/{appkey}/videos", "GET", "com.github.wens.netty.web.app.VideoController.videos(java.lang.String)");
        routeMatch.addRouter("/api/{appkey}/video/{videoid}", "GET", "com.github.wens.netty.web.app.VideoController.video");
        routeMatch.addRouter("/api/{appkey:[A-Z]+}/videos/{pagesize:\\d+}", "GET", "com.github.wens.netty.web.app.VideoController.videos(java.lang.String, java.lang.String)");

        Assert.assertNotNull(routeMatch.match("GET", "/api/index"));
        Assert.assertNull(routeMatch.match("POST", "/api/index"));
        Assert.assertNull(routeMatch.match("GET", "/api/inde"));
        Assert.assertNull(routeMatch.match("GET", "/api/index2"));
        Assert.assertNotNull(routeMatch.match("GET", "/api/dd/d/like/11"));
        Assert.assertNull(routeMatch.match("GET", "/api/dd/d/like/11/55"));
        Assert.assertNotNull(routeMatch.match("GET", "/api/appkey01/videos"));
        Assert.assertNull(routeMatch.match("GET", "/api/appkey01/d/videos"));
        Assert.assertNotNull(routeMatch.match("GET", "/api/appkey01/video/88fdfdf"));
        Assert.assertNull(routeMatch.match("GET", "/api/appkey01/d/video/88fdfdf"));
        Assert.assertNotNull(routeMatch.match("GET", "/api/APPKEY/videos/2222"));
        Assert.assertNull(routeMatch.match("GET", "/api/appkey/videos/2222"));
        Assert.assertNull(routeMatch.match("GET", "/api/APPKEY/videos/2222id"));
        Assert.assertNull(routeMatch.match("GET", "/api/APPKEY/videos/2222/id"));

    }


}
