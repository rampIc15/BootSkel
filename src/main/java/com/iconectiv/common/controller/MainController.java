package com.iconectiv.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by rgovindprasad on 7/5/2015.
 */
@Controller
public class MainController {

    @RequestMapping("/jsptest")
    public String test(ModelAndView modelAndView) {

        return "jsp-spring-boot";
    }

}

