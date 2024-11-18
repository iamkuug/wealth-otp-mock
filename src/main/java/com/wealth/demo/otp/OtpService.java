package com.wealth.demo.otp;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.wealth.demo.dto.WhatsappRequest;
import com.wealth.demo.ex.BadRequestException;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.wapp.access.token}")
    private String accessToken;

    @Value("${spring.wapp.business.id}")
    private String businessId;

    @Value("${spring.wapp.api.url}")
    private String baseURL;

    public Otp getOtp(String token, String otpCode) {
        Otp otp = otpRepository.findById(new OtpId(otpCode, token)).orElse(null);
        
        System.out.println(otp);

        if (otp == null) {
            throw new BadRequestException("Invalid Token or OTP Code");
        }

        return otp;
    }

    public Otp saveOtp(Otp otp) {
        return otpRepository.save(otp);
    }

    public String generateOtpCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder otpCode = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            otpCode.append(random.nextInt(10));
        }

        return otpCode.toString();
    }

    public String generateToken() {
        UUID uuid = UUID.randomUUID();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuid.toString().getBytes());
    }

    public Date generateExpiryDate(int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    public void sendOtp(String phoneNumber, String otpCode) {
        this.sendOtpAuthTemplate(otpCode, phoneNumber);
    }

    public boolean isPhoneNumberRegisteredOnWhatsapp(String phoneNumber) {
        return this.sendVerifyTemplate(phoneNumber);
    }

    private ResponseEntity<?> sendOtpAuthTemplate(String otpCode, String phoneNumber) {
        final String endpointURL = baseURL + businessId.trim() + "/messages";

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
                                                        .text(otpCode)
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
                                                        .text(otpCode)
                                                        .build()))
                                        .build()))
                        .build())
                .build();

        HttpEntity<WhatsappRequest> requestEntity = new HttpEntity<>(whatsappRequest,
                headers);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(null, null,
                400);

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

    private boolean sendVerifyTemplate(String phoneNumber) {
        final String endpointURL = baseURL + businessId.trim() + "/messages";

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

        HttpEntity<WhatsappRequest> requestEntity = new HttpEntity<>(whatsappRequest,
                headers);

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