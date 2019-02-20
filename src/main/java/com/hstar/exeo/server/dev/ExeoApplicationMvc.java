package com.hstar.exeo.server.dev;

import com.hstar.exeo.server.ExeoApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * MVC Configuration that maps to static assets.
 * Replaced by NGINX in production
 *
 * Created by Saswat on 8/6/2016.
 */
@Profile(ExeoApplication.DEVELOPMENT_PROFILE)
@Configuration
@EnableWebMvc
public class ExeoApplicationMvc extends WebMvcConfigurerAdapter {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("*").addResourceLocations("file:build-webapp/").resourceChain(true).addResolver(new PathResourceResolver() {
            @Override
            public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
                if(requestPath.isEmpty() || requestPath.equals("/")) {
                    requestPath = "index.html";
                }
                Resource r = super.resolveResource(request,requestPath, locations, chain);
                if(r == null && !requestPath.contains(".")) {
                    r = super.resolveResource(request, requestPath+".html", locations, chain);
                }
                return r;
            }
        });
    }

}
