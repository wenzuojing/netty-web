package com.github.wens.netty.web.impl;

import com.github.wens.netty.web.ObjectFactory;
import com.github.wens.netty.web.util.ClassUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wens on 15-5-15.
 */
public class DefaultObjectFactory implements ObjectFactory {

    private ConcurrentHashMap<String, Object> objectCache = new ConcurrentHashMap<String, Object>();

    public Object instance(String className, boolean singleton) {

        if (singleton) {
            return getFromObjectCache(className);
        }

        return null;
    }

    private Object getFromObjectCache(String className) {

        Object o = objectCache.get(className);

        if (o != null) {
            return o;
        }

        o = ClassUtils.newInstance(className);

        Object oldObject = objectCache.putIfAbsent(className, o);

        if (oldObject != null) {
            o = oldObject;
        }

        return o;

    }


}
