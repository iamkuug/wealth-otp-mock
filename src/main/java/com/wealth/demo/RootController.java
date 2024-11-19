package com.wealth.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestController
public class RootController {

    @GetMapping("/")
    public String getRoot() {
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { background-color: #121212; color: #ffffff; font-family: Arial, sans-serif; }" +
                "a { color: #bb86fc; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<p>Access the documentation <a href='/swagger-ui.html'>here</a>.</p>" +
                "</body>" +
                "</html>";
    }
}