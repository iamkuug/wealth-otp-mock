package com.wealth.demo.msg;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.wealth.demo.dto.WhatsappTextMessageRequest;
import com.wealth.demo.ex.MessageSendingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
public class MessageService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.wapp.access.token}")
    private String accessToken;

    @Value("${spring.wapp.business.id}")
    private String businessId;

    @Value("${spring.wapp.api.url}")
    private String baseURL;

    public void send(String phoneNumber, String messageBody) {
        this.sendTextMessage(phoneNumber, messageBody);
    }

    private ResponseEntity<?> sendTextMessage(String phoneNumber, String messageBody) {
        final String endpointURL = baseURL + businessId.trim() + "/messages";

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken.trim());
        headers.set("Content-Type", "application/json");

        // Build payload
        WhatsappTextMessageRequest whatsappRequest = WhatsappTextMessageRequest.builder()
                .messaging_product("whatsapp")
                .recipient_type("individual")
                .to(phoneNumber)
                .type("text")
                .text(WhatsappTextMessageRequest.Text.builder()
                        .preview_url(true) // Set to true or false based on your requirement
                        .body(messageBody)
                        .build())
                .build();

        HttpEntity<WhatsappTextMessageRequest> requestEntity = new HttpEntity<>(whatsappRequest,
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
                throw new MessageSendingException("Failed to send message: " + responseEntity.getBody());
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new MessageSendingException("Bad request: " + "Recipient's phone number is not in allowed list");
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new MessageSendingException("Sending Service: Unauthorized - " + e.getResponseBodyAsString());
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new MessageSendingException("Sending Service: Forbidden - " + e.getResponseBodyAsString());
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new MessageSendingException("Sending Service: Not found - " + e.getResponseBodyAsString());
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new MessageSendingException(
                        "Sending Service: Too many requests - " + e.getResponseBodyAsString());
            } else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                throw new MessageSendingException(
                        "Sending Service: Internal server error - " + e.getResponseBodyAsString());
            } else if (e.getStatusCode() == HttpStatus.BAD_GATEWAY) {
                throw new MessageSendingException("Sending Service: Bad gateway - " + e.getResponseBodyAsString());
            } else if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                throw new MessageSendingException(
                        "Sending Service: Service unavailable - " + e.getResponseBodyAsString());
            } else {
                throw new MessageSendingException(
                        "Sending Service: Error sending OTP - " + e.getResponseBodyAsString());
            }
        } catch (RestClientException rce) {
            throw new MessageSendingException("Error sending OTP: " + rce.getMessage());
        } catch (URISyntaxException use) {
            throw new MessageSendingException("Error parsing URI: " + endpointURL + " - " + use.getMessage());
        }

    }
}
