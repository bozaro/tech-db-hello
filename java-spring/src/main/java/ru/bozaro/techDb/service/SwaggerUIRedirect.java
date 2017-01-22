package ru.bozaro.techDb.service;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SwaggerUIRedirect {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String redirect(HttpServletRequest request) {
        return "redirect:swagger-ui.html";
    }
}