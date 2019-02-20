package com.hstar.exeo.server.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hstar.exeo.objects.ws.WSEvent;
import com.hstar.exeo.server.websockets.annotations.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by Saswat on 8/11/2015.
 */
public class WSExeoHandler extends WSHandler {

    private static HashMap<String, WSMethod> wsControllerActions;
    private static HashMap<String, WSMethod> redisControllerActions;

    private final ApplicationContext applicationContext;
    private HashMap<Class, Object> initializedControllers = new HashMap<>();

    static {
        //magic! some reflection to find the wscontrollers so we can reflectively call them
        HashMap<String, WSMethod> wsControllerActions = new HashMap<>();
        HashMap<String, WSMethod> redisControllerActions = new HashMap<>();

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(WSController.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(RedisController.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.hstar.exeo.server")) {
            try {
                Class controller = Class.forName(bd.getBeanClassName());

                for(Method m : controller.getDeclaredMethods()) {
                    HashMap<String, WSMethod> actions;
                    RequestMappingAuthState authState;
                    String key;
                    if(m.isAnnotationPresent(RequestWSMapping.class)) {
                        RequestWSMapping annotation = m.getAnnotation(RequestWSMapping.class);
                        key = annotation.value().toString();
                        authState = annotation.state();
                        actions = wsControllerActions;
                    } else if(m.isAnnotationPresent(RequestRedisMapping.class)) {
                        RequestRedisMapping annotation = m.getAnnotation(RequestRedisMapping.class);
                        key = annotation.value().toString();
                        authState = annotation.state();
                        actions = redisControllerActions;
                    } else {
                        continue;
                    }

                    if(actions.containsKey(key)) {
                        System.err.println("DUPLICATE ACTION: "+key);
                        continue;//todo ERROR!!!
                    }

                    actions.put(key, new WSMethod(m, authState, controller));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        WSExeoHandler.wsControllerActions = wsControllerActions;
        WSExeoHandler.redisControllerActions = redisControllerActions;
    }

    public WSExeoHandler(RedisTemplate<String, String> redis, RedisMessageListenerContainer listenerContainer, ApplicationContext applicationContext) {
        super(redis, listenerContainer);

        this.applicationContext = applicationContext;
    }

    @Override
    protected void afterConnectionStarted(WebSocketSession session) throws Exception {}

    @Override
    protected void afterConnectionEnded(WebSocketSession session, CloseStatus status) throws Exception {}

    @Override
    protected void handleMessage(WebSocketSession session, WSEvent request) throws Exception {
        WSContext context = new WSContext(session, this, request);
        if(!wsControllerActions.containsKey(context.request.getName())) {
            return; //todo report error
        }
        callReflectionAction(wsControllerActions.get(context.request.getName()), context);
    }

    @Override
    protected void handleRedisMessage(WebSocketSession session, WSEvent request) throws Exception {
        WSContext context = new WSContext(session, this, request);
        WSMethod method = null;
        for(Map.Entry<String, WSMethod> e : redisControllerActions.entrySet()) {
            if(request.getName().startsWith(e.getKey())) {
                method = e.getValue();
                break;
            }
        }
        if(method == null) {
            return; //todo report error
        }
        callReflectionAction(method, context);
    }

    private boolean callReflectionAction(WSMethod method, WSContext context) throws InvocationTargetException, IllegalAccessException {
        boolean invalidState = false;
        if(context.isQuickMode() && method.authState.allowsQuickMode()) {
            //todo report error
            invalidState = true;
        } else {
            switch (method.authState) {
                case ANY:
                    //no protections for the any state
                    break;
                case NOT_LOGGED_IN:
                    if (context.hasUser() || context.hasDevice()) {
                        //todo report error
                        invalidState = true;
                    }
                    break;
                case PENDING_DEVICE_REGISTRATION:
                    if (!context.hasUser() || context.hasDevice()) {
                        //todo report error
                        invalidState = true;
                    }
                    break;
                case AUTHORIZED:
                    if (!context.hasUser() || !context.hasDevice()) {
                        //todo report error
                        invalidState = true;
                    }
                    break;
                case QUICK:
                    if (!context.isQuickMode()) {
                        //todo report error
                        invalidState = true;
                    }
            }
        }

        //close our session if an invalid action is attempted
        if(invalidState) {
            context.closeSession();
            return false;
        }

        Class parameterTypes[] = method.method.getParameterTypes();
        if(parameterTypes.length != 1 || parameterTypes[0] != WSContext.class) {
            return false; //todo report error
        }

        Object controller;
        if(initializedControllers.containsKey(method.controller)) {
            controller = initializedControllers.get(method.controller);
        } else {
            controller = applicationContext.getAutowireCapableBeanFactory().createBean(method.controller);
            initializedControllers.put(method.controller, controller);
        }

        method.method.invoke(controller, context);

        return true;
    }

    protected void sendMessage(WebSocketSession session, WSEvent message) throws IOException {
        sendMessage(session, new ObjectMapper().writeValueAsString(message));
    }

    private static class WSMethod {
        final Method method;
        final RequestMappingAuthState authState;
        final Class<Object> controller;

        private WSMethod(Method method, RequestMappingAuthState authState, Class<Object> controller) {
            this.method = method;
            this.authState = authState;
            this.controller = controller;
        }
    }
}
