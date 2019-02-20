package com.hstar.exeo.server.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Saswat on 10/11/2015.
 */
@RestController
public class PairRESTController {

    @Autowired private RedisTemplate<String, String> redis;

    @RequestMapping(value = "/api/pair", method = RequestMethod.POST)
    public String pair(@RequestParam("comp1") String comp1, @RequestParam("comp2") String comp2) {
        System.out.println("paired: "+comp1+" - "+comp2);
        return "paired";
    }

}
