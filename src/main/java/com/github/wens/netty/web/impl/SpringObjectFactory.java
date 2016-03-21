package com.github.wens.netty.web.impl;

import com.github.wens.netty.web.ObjectFactory;
import com.github.wens.netty.web.WebException;
import com.github.wens.netty.web.util.ClassUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * integrated with spring
 * Created by wens on 15-9-1.
 */
public class SpringObjectFactory implements ObjectFactory {

    protected ApplicationContext appContext;
    protected AutowireCapableBeanFactory autoWiringFactory;
    private ConcurrentHashMap<String, Object> objectCache = new ConcurrentHashMap<String, Object>();

    public SpringObjectFactory(ApplicationContext appContext) {
        this.appContext = appContext;
        autoWiringFactory = findAutoWiringBeanFactory(this.appContext);
    }


    @Override
    public Object instance(String className, boolean singleton) {

        if (singleton) {
            return getFromObjectCache(className);
        }

        try {
            return buildBean(ClassUtils.loadClass(className));
        } catch (Exception e) {
            throw new WebException("instance class fail : " + className, e);
        }
    }

    private Object getFromObjectCache(String className) {

        Object o = objectCache.get(className);

        if (o != null) {
            return o;
        }

        try {
            o = buildBean(ClassUtils.loadClass(className));
        } catch (Exception e) {
            throw new WebException("instance class fail : " + className, e);
        }

        Object oldObject = objectCache.putIfAbsent(className, o);

        if (oldObject != null) {
            o = oldObject;
        }

        return o;

    }


    protected AutowireCapableBeanFactory findAutoWiringBeanFactory(ApplicationContext context) {
        if (context instanceof AutowireCapableBeanFactory) {
            return (AutowireCapableBeanFactory) context;
        } else if (context instanceof ConfigurableApplicationContext) {
            return ((ConfigurableApplicationContext) context).getBeanFactory();
        } else if (context.getParent() != null) {
            return findAutoWiringBeanFactory(context.getParent());
        }
        return null;
    }

    public Object buildBean(Class clazz) throws Exception {
        Object bean;
        bean = autoWiringFactory.autowire(clazz, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
        bean = autoWiringFactory.applyBeanPostProcessorsBeforeInitialization(bean, bean.getClass().getName());
        bean = autoWiringFactory.applyBeanPostProcessorsAfterInitialization(bean, bean.getClass().getName());
        return bean;
    }


}
