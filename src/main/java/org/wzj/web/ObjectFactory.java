package org.wzj.web;

/**
 * Created by wens on 15-5-15.
 */
public interface ObjectFactory {
    Object instance(String className, boolean singleton);
}
