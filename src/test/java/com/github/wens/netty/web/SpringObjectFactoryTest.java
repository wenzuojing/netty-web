package com.github.wens.netty.web;

import com.github.wens.netty.web.impl.SpringObjectFactory;
import com.github.wens.netty.web.spring.TestController;
import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by wens on 15-9-1.
 */
public class SpringObjectFactoryTest extends TestCase {


    public void test_0() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");


        SpringObjectFactory springObjectFactory = new SpringObjectFactory(context);
        TestController c = (TestController) springObjectFactory.instance("com.github.wens.netty.web.spring.TestController", true);
        c.test();

    }

}
