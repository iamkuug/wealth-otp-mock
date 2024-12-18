package com.icsecurities.app.otp;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.icsecurities.app.dto.WhatsappTemplateMessageRequest;
import com.icsecurities.app.ex.BadRequestException;
import com.icsecurities.app.ex.OtpSendingException;

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
        Otp otp = otpRepository.findMostRecentOtp(token, otpCode);

        if (otp == null) {
            throw new BadRequestException("Invalid Token or OTP Code");
        }

        return otp;
    }

    public Otp saveOtp(Otp otp) {
        return otpRepository.save(otp);
    }

    public void purgeAllExpiredOtps(String phoneNumber) {
        List<Otp> otps = otpRepository.findByPhoneNumber(phoneNumber);

        for (Otp otp : otps) {
            if (otp.getCreatedAt().before(new Date())) {
                otpRepository.delete(otp);
            }
        }
    }

    public void deleteOtp(Otp otp) {
        otpRepository.delete(otp);
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
        WhatsappTemplateMessageRequest whatsappRequest = this.buildWhatsappTemplateMessageRequest(otpCode, phoneNumber);

        HttpEntity<WhatsappTemplateMessageRequest> requestEntity = new HttpEntity<>(whatsappRequest,
                headers);

        // Send request
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    new URI(endpointURL),
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            System.out.println(responseEntity.getStatusCode());

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity;
            } else {
                throw new OtpSendingException("Failed to send OTP: " + responseEntity.getBody());
            }
        } catch (HttpClientErrorException e) {
            HttpStatus statusCode = HttpStatus.valueOf(e.getStatusCode().value());

            switch (statusCode) {
                case BAD_REQUEST:
                    throw new OtpSendingException("Sending Service: " + "Recipient phone number is not in allowed list");
                case UNAUTHORIZED:
                    throw new OtpSendingException("Sending Service: Unauthorized - " + e.getResponseBodyAsString());
                case FORBIDDEN:
                    throw new OtpSendingException("Sending Service: Forbidden - " + e.getResponseBodyAsString());
                case NOT_FOUND:
                    throw new OtpSendingException("Sending Service: Not found - " + e.getResponseBodyAsString());
                case TOO_MANY_REQUESTS:
                    throw new OtpSendingException(
                            "Sending Service: Too many requests - " + e.getResponseBodyAsString());
                case INTERNAL_SERVER_ERROR:
                    throw new OtpSendingException(
                            "Sending Service: Internal server error - " + e.getResponseBodyAsString());
                case BAD_GATEWAY:
                    throw new OtpSendingException("Sending Service: Bad gateway - " + e.getResponseBodyAsString());
                case SERVICE_UNAVAILABLE:
                    throw new OtpSendingException(
                            "Sending Service: Service unavailable - " + e.getResponseBodyAsString());
                default:
                    throw new OtpSendingException(
                            "Sending Service: Error sending OTP - " + e.getResponseBodyAsString());
            }

        } catch (RestClientException rce) {
            throw new OtpSendingException("Error sending OTP: " + rce.getMessage());
        } catch (URISyntaxException use) {
            throw new OtpSendingException("Error parsing URI: " + endpointURL + " - " + use.getMessage());
        }

    }

    private boolean sendVerifyTemplate(String phoneNumber) {
        final String endpointURL = baseURL + businessId.trim() + "/messages";

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken.trim());
        headers.set("Content-Type", "application/json");

        // Build payload
        WhatsappTemplateMessageRequest whatsappRequest = WhatsappTemplateMessageRequest.builder()
                .messaging_product("whatsapp")
                .recipient_type("individual")
                .to(phoneNumber)
                .type("template")
                .template(WhatsappTemplateMessageRequest.Template.builder()
                        .name("hello_world")
                        .language(WhatsappTemplateMessageRequest.Template.Language.builder()
                                .code("en_US")
                                .build())
                        .components(Collections.singletonList(
                                WhatsappTemplateMessageRequest.Template.Component.builder()
                                        .type("body")
                                        .parameters(Collections.emptyList())
                                        .build()))
                        .build())
                .build();

        HttpEntity<WhatsappTemplateMessageRequest> requestEntity = new HttpEntity<>(whatsappRequest,
                headers);

        // Send request
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    new URI(endpointURL),
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            System.out.println(responseEntity.getStatusCode());

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return true;
            } else {
                throw new OtpSendingException("Failed to send OTP: " + responseEntity.getBody());
            }
        } catch (HttpClientErrorException e) {
            HttpStatus statusCode = HttpStatus.valueOf(e.getStatusCode().value());

            switch (statusCode) {
                case BAD_REQUEST:
                    throw new OtpSendingException("Bad request: " + "Recipient phone number is not in allowed list");
                case UNAUTHORIZED:
                    throw new OtpSendingException("Sending Service: Unauthorized - " + e.getResponseBodyAsString());
                case FORBIDDEN:
                    throw new OtpSendingException("Sending Service: Forbidden - " + e.getResponseBodyAsString());
                case NOT_FOUND:
                    throw new OtpSendingException("Sending Service: Not found - " + e.getResponseBodyAsString());
                case TOO_MANY_REQUESTS:
                    throw new OtpSendingException(
                            "Sending Service: Too many requests - " + e.getResponseBodyAsString());
                case INTERNAL_SERVER_ERROR:
                    throw new OtpSendingException(
                            "Sending Service: Internal server error - " + e.getResponseBodyAsString());
                case BAD_GATEWAY:
                    throw new OtpSendingException("Sending Service: Bad gateway - " + e.getResponseBodyAsString());
                case SERVICE_UNAVAILABLE:
                    throw new OtpSendingException(
                            "Sending Service: Service unavailable - " + e.getResponseBodyAsString());
                default:
                    throw new OtpSendingException(
                            "Sending Service: Error sending OTP - " + e.getResponseBodyAsString());
            }

        } catch (RestClientException e) {
            throw new OtpSendingException("Error sending OTP: " + e.getMessage());
        } catch (URISyntaxException e) {
            throw new OtpSendingException("Error parsing URI: " + endpointURL + " - " + e.getMessage());
        }

    }

    private WhatsappTemplateMessageRequest buildWhatsappTemplateMessageRequest(String otpCode, String phoneNumber) {
        WhatsappTemplateMessageRequest.Template.Language language = WhatsappTemplateMessageRequest.Template.Language
                .builder()
                .code("en_US")
                .build();

        WhatsappTemplateMessageRequest.Template.Component bodyComponent = WhatsappTemplateMessageRequest.Template.Component
                .builder()
                .type("body")
                .parameters(Collections.singletonList(
                        WhatsappTemplateMessageRequest.Template.Component.Parameter.builder()
                                .type("text")
                                .text(otpCode)
                                .build()))
                .build();

        WhatsappTemplateMessageRequest.Template.Component buttonComponent = WhatsappTemplateMessageRequest.Template.Component
                .builder()
                .type("button")
                .sub_type("url")
                .index(0)
                .parameters(Collections.singletonList(
                        WhatsappTemplateMessageRequest.Template.Component.Parameter.builder()
                                .type("text")
                                .text(otpCode)
                                .build()))
                .build();

        WhatsappTemplateMessageRequest.Template template = WhatsappTemplateMessageRequest.Template.builder()
                .name("ic_wealth_passcode")
                .language(language)
                .components(Arrays.asList(bodyComponent, buttonComponent))
                .build();

        WhatsappTemplateMessageRequest whatsappRequest = WhatsappTemplateMessageRequest.builder()
                .messaging_product("whatsapp")
                .recipient_type("individual")
                .to(phoneNumber)
                .type("template")
                .template(template)
                .build();

        return whatsappRequest;

    }
}