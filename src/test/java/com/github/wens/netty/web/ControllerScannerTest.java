package com.github.wens.netty.web;

import com.github.wens.netty.web.impl.DefaultObjectFactory;
import com.github.wens.netty.web.route.RouteMatcher;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by wens on 15-9-1.
 */
public class ControllerScannerTest {

    @Test
    public void test_0() {

        RouteMatcher routeMatch = new RouteMatcher("");
        ControllerScanner controllerScanner = new ControllerScanner(routeMatch, new DefaultObjectFactory());

        controllerScanner.scanControllers("com.github.wens.netty.web.app");


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
