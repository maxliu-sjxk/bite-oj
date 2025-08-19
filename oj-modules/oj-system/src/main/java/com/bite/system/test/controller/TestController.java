package com.bite.system.test.controller;

import com.bite.common.redis.service.RedisService;
import com.bite.system.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RedisService redisService;



    @RequestMapping("/redis")
    public String testRedis() {
        redisService.setCacheObject("testString", "String");
        String testString = redisService.getCacheObject("testString", String.class);
        System.out.println(testString);

        redisService.setCacheObject("testInteger", 1);
        Integer testInteger = redisService.getCacheObject("testInteger", Integer.class);
        System.out.println(testInteger);

        redisService.setCacheObject("testObject", new SysUser());
        SysUser user = redisService.getCacheObject("testObject", SysUser.class);
        System.out.println(user);

        System.out.println();
        return "test";
    }
}
