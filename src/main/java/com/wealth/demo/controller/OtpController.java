package com.wealth.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.wealth.demo.dto.RegisterRequest;

@RestController
@RequestMapping("/api")
public class OtpController {
    
    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.wapp.access.token}")
    private String accessToken;

    @Value("${spring.wapp.business.id}")  
    private String businessId; 

    @Value("${spring.wapp.api.url}")    
    private String apiURL;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        String phoneNumber = request.getPhoneNumber();
        
        return "OTP sent to " + phoneNumber + accessToken + businessId+ apiURL;
        
    }
}
