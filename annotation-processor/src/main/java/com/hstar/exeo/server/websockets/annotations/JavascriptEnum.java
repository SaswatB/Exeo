package com.hstar.exeo.server.websockets.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Saswat on 10/28/2016.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JavascriptEnum {
    String value() default "";//name, default is the name of the file
}
