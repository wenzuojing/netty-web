package com.github.wens.netty.web.spring;

import javax.annotation.Resource;

/**
 * Created by wens on 15-9-1.
 */
public class TestController {

    @Resource(name = "testBean")
    private TestBean testBean;


    public void test() {
        System.out.println(testBean.test());
    }

}
