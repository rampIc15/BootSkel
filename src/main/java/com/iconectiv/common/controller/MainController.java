package com.iconectiv.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by rgovindprasad on 7/5/2015.
 */
@Controller
public class MainController {

    @Autowired
    Config config;


    @RequestMapping("/jsptest")
    public String test(ModelAndView modelAndView) {
        System.out.println("Query : " + config.getQueryFiles());
        System.out.println("version : " + config.getVersion());
        return "jsp-spring-boot";
    }

}

