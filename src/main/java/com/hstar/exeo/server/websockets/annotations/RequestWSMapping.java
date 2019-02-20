package com.hstar.exeo.server.websockets.annotations;

import com.hstar.exeo.objects.ws.WSEName;

import java.lang.annotation.*;

/**
 * Created by Saswat on 10/26/2016.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestWSMapping {
    WSEName value();
    RequestMappingAuthState state();
}
