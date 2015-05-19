package org.wzj.web.annotaction;

import org.wzj.web.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wens on 15-5-14.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Router {

    String value();

    HttpMethod method() default HttpMethod.GET;


}
