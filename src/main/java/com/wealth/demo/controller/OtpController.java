package com.wealth.demo.controller;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.wealth.demo.dto.MetaRequest;
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
        public String registerUser(@RequestBody RegisterRequest request) {
                String phoneNumber = request.getPhoneNumber();
                String otp = "1234";
                String trimmedApiURL = apiURL.trim();
                String trimmedBusinessId = businessId.trim();
                String endpoint = trimmedApiURL + trimmedBusinessId + "/messages";

                MetaRequest metaRequest = MetaRequest.builder()
                                .messaging_product("whatsapp")
                                .recipient_type("individual")
                                .to(phoneNumber)
                                .type("template")
                                .template(MetaRequest.Template.builder()
                                                .name("wealth_otp_test")
                                                .language(MetaRequest.Template.Language.builder()
                                                                .code("en_US")
                                                                .build())
                                                .components(Arrays.asList(
                                                                MetaRequest.Template.Component.builder()
                                                                                .type("body")
                                                                                .parameters(Collections.singletonList(
                                                                                                MetaRequest.Template.Component.Parameter
                                                                                                                .builder()
                                                                                                                .type("text")
                                                                                                                .text(otp)
                                                                                                                .build()))
                                                                                .build(),
                                                                MetaRequest.Template.Component.builder()
                                                                                .type("button")
                                                                                .sub_type("url")
                                                                                .index(0)
                                                                                .parameters(Collections.singletonList(
                                                                                                MetaRequest.Template.Component.Parameter
                                                                                                                .builder()
                                                                                                                .type("text")
                                                                                                                .text(otp)
                                                                                                                .build()))
                                                                                .build()))
                                                .build())
                                .build();

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + accessToken.trim());
                headers.set("Content-Type", "application/json");

                HttpEntity<MetaRequest> entity = new HttpEntity<>(metaRequest, headers);
                System.out.println("API URL: " + endpoint);

                ResponseEntity<String> response = new ResponseEntity<>(null, null, 404);
                try {
                        URI uri = new URI(endpoint);
                        response = restTemplate.exchange(
                                        uri, HttpMethod.POST,
                                        entity,
                                        String.class);

                        System.out.println("something fishy");

                        System.out.println("API Response: " + response.getStatusCode());
                } catch (RestClientException | URISyntaxException e) {
                        e.printStackTrace();
                }

                return "New response     ";
        }

        @PostMapping("/resend-otp")
        public String resendOtp(@RequestBody RegisterRequest request) {
                return "OTP has been resent";
        }

        // Just incase we need to check if the phone number is already registered
        @PostMapping("/check-phone-number")
        public String checkPhoneNumber() {
                return "Phone number is not registered";
        }

        @PostMapping("/verify-otp")
        public String verifyOtp(@RequestBody RegisterRequest request) {
                String phoneNumber = request.getPhoneNumber();
                String otp = request.getOtp();
                String trimmedApiURL = apiURL.trim();
                String trimmedBusinessId = businessId.trim();
                String endpoint = trimmedApiURL + trimmedBusinessId + "/messages";
        }
}
