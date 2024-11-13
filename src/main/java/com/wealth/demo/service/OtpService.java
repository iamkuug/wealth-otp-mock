package com.wealth.demo.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.wealth.demo.dto.WhatsappRequest;
import com.wealth.demo.entity.Otp;
import com.wealth.demo.repository.OtpRepository;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.wapp.access.token}")
    private String accessToken;

    public Otp saveOtp(Otp otp) {
        return otpRepository.save(otp);
    }

    public Otp getOtpByPhoneNumber(String phoneNumber) {
        return otpRepository.findByAccountPhoneNumber(phoneNumber);
    }

    @Transactional
    public void deleteOtp(UUID id) {
        otpRepository.deleteById(id);
    }

    @Transactional
    public void deleteOtpByAccountId(UUID accountId) {
        otpRepository.deleteByAccountId(accountId);
    }

    public String generateOtp() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000); // Generates a random number between 1000 and 9999
        return String.valueOf(otp);
    }

    public Boolean isPhoneNumberRegisteredOnWhatsapp(String phoneNumber, String endpointURL) {
        return this.sendVerifyTemplate(phoneNumber, endpointURL);
    }

    public ResponseEntity<String> sendOtpTemplate(String otp, String phoneNumber, String endpointURL) {
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken.trim());
        headers.set("Content-Type", "application/json");

        // Build payload
        WhatsappRequest whatsappRequest = WhatsappRequest.builder()
                .messaging_product("whatsapp")
                .recipient_type("individual")
                .to(phoneNumber)
                .type("template")
                .template(WhatsappRequest.Template.builder()
                        .name("wealth_otp_test")
                        .language(WhatsappRequest.Template.Language.builder()
                                .code("en_US")
                                .build())
                        .components(Arrays.asList(
                                WhatsappRequest.Template.Component.builder()
                                        .type("body")
                                        .parameters(Collections.singletonList(
                                                WhatsappRequest.Template.Component.Parameter
                                                        .builder()
                                                        .type("text")
                                                        .text(otp)
                                                        .build()))
                                        .build(),
                                WhatsappRequest.Template.Component.builder()
                                        .type("button")
                                        .sub_type("url")
                                        .index(0)
                                        .parameters(Collections.singletonList(
                                                WhatsappRequest.Template.Component.Parameter
                                                        .builder()
                                                        .type("text")
                                                        .text(otp)
                                                        .build()))
                                        .build()))
                        .build())
                .build();

        HttpEntity<WhatsappRequest> requestEntity = new HttpEntity<>(whatsappRequest, headers);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(null, null, 400);

        // Send request
        try {
            URI uri = new URI(endpointURL);
            responseEntity = restTemplate.exchange(
                    uri, HttpMethod.POST,
                    requestEntity,
                    String.class);

        } catch (RestClientException rce) {
            System.out.println("Error sending OTP");
            System.out.println(rce.getMessage());
        } catch (URISyntaxException use) {
            System.out.println("Error parsing uri; " + endpointURL);
            System.out.println(use.getMessage());
        }

        return responseEntity;
    }

    public boolean sendVerifyTemplate(String phoneNumber, String endpointURL) {
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken.trim());
        headers.set("Content-Type", "application/json");

        // Build payload
        WhatsappRequest whatsappRequest = WhatsappRequest.builder()
                .messaging_product("whatsapp")
                .recipient_type("individual")
                .to(phoneNumber)
                .type("template")
                .template(WhatsappRequest.Template.builder()
                        .name("hello_world")
                        .language(WhatsappRequest.Template.Language.builder()
                                .code("en_US")
                                .build())
                        .components(Collections.singletonList(
                                WhatsappRequest.Template.Component.builder()
                                        .type("body")
                                        .parameters(Collections.emptyList())
                                        .build()))
                        .build())
                .build();

        HttpEntity<WhatsappRequest> requestEntity = new HttpEntity<>(whatsappRequest, headers);
        // ResponseEntity<String> responseEntity = new ResponseEntity<>(null, null, 400);

        // Send request
        try {
            URI uri = new URI(endpointURL);
            restTemplate.exchange(
                    uri, HttpMethod.POST,
                    requestEntity,
                    String.class);

        } catch (RestClientException rce) {
            System.out.println("Error sending verification template");
            System.out.println(rce.getMessage());
            return false;
        } catch (URISyntaxException use) {
            System.out.println("Error parsing uri; " + endpointURL);
            System.out.println(use.getMessage());
            return false;
        }

        return true;
    }
}