package com.wealth.demo.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wealth.demo.dto.GenericResponse;
import com.wealth.demo.dto.RegisterRequest;
import com.wealth.demo.dto.VerifyRequest;
import com.wealth.demo.entity.Account;
import com.wealth.demo.entity.Otp;
import com.wealth.demo.service.AccountService;
import com.wealth.demo.service.OtpService;

@RestController
@RequestMapping("/api")
public class RootController {

        @Autowired
        private OtpService otpService;

        @Autowired
        private AccountService accountService;

        @Value("${spring.wapp.business.id}")
        private String businessId;

        @Value("${spring.wapp.api.url}")
        private String baseURL;

        @PostMapping("/register")
        public ResponseEntity<GenericResponse> registerUser(@RequestBody RegisterRequest request) {
                String phoneNumber = request.getPhoneNumber();
                final String messageEndpointURL = baseURL + businessId.trim() + "/messages";

                if (accountService.getAccountByPhoneNumber(phoneNumber) != null) {
                        GenericResponse response = new GenericResponse("Phone number already registered");
                        return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
                }

                if (phoneNumber.length() != 12) {
                        GenericResponse response = new GenericResponse("Invalid phone number");
                        return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
                }

                if (!otpService.isPhoneNumberRegisteredOnWhatsapp(phoneNumber, messageEndpointURL)) {
                        GenericResponse response = new GenericResponse(
                                        "Invalid Whatsapp Number. Number has to be registered with Whatsapp");
                        return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);

                }

                Account newAccount = new Account(phoneNumber);
                Otp newOtp = new Otp(otpService.generateOtp(), newAccount);

                accountService.saveAccount(newAccount);
                otpService.saveOtp(newOtp);

                ResponseEntity<String> messageEndpointResponse = otpService.sendOtpTemplate(newOtp.getOtp(),
                                phoneNumber, messageEndpointURL);

                if (messageEndpointResponse.getStatusCode() != HttpStatus.OK) {
                        GenericResponse response = new GenericResponse("Failed to send OTP");
                        return new ResponseEntity<GenericResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }

                GenericResponse response = new GenericResponse("Registration Successful. OTP sent successfully");
                return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
        }

        @PostMapping("/login")
        public ResponseEntity<GenericResponse> loginUser(@RequestBody RegisterRequest request) {
                String phoneNumber = request.getPhoneNumber();
                Account account = accountService.getAccountByPhoneNumber(phoneNumber);
                final String messageEndpointURL = baseURL + businessId.trim() + "/messages";

                if (account == null) {
                        GenericResponse response = new GenericResponse("No account found with this phone number");
                        return new ResponseEntity<GenericResponse>(response, HttpStatus.NOT_FOUND);
                }

                Otp newOtp = new Otp(otpService.generateOtp(), account);
                otpService.deleteOtpByAccountId(account.getId());
                otpService.saveOtp(newOtp);

                ResponseEntity<String> messageEndpointResponse = otpService.sendOtpTemplate(newOtp.getOtp(),
                                phoneNumber, messageEndpointURL);

                if (messageEndpointResponse.getStatusCode() != HttpStatus.OK) {
                        GenericResponse response = new GenericResponse("Failed to send OTP");
                        return new ResponseEntity<GenericResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }

                GenericResponse response = new GenericResponse("OTP sent successfully. Check Whatsapp for OTP");
                return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
        }


        @PostMapping("/verify-otp")
        public ResponseEntity<GenericResponse> verifyOtp(@RequestBody VerifyRequest request) {
                String otpGot = request.getOtp();
                String phoneNumber = request.getPhoneNumber();
                Integer otpExpirationMinutes = 10;

                Otp otpExpected = otpService.getOtpByPhoneNumber(phoneNumber);

                if (otpExpected == null) {
                        GenericResponse response = new GenericResponse("OTP not found/Accout not found");
                        return new ResponseEntity<GenericResponse>(response, HttpStatus.NOT_FOUND);
                }

                Date otpCreatedAt = otpExpected.getCreatedAt();
                Date now = new Date();
                long diff = now.getTime() - otpCreatedAt.getTime();
                long diffMinutes = diff / (60 * 1000) % 60;

                if (diffMinutes > otpExpirationMinutes) {
                        GenericResponse response = new GenericResponse("OTP has expired");
                        return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
                }

                if (!otpGot.equals(otpExpected.getOtp())) {
                        otpService.deleteOtp(otpExpected.getId());
                        GenericResponse response = new GenericResponse("OTP is incorrect");
                        return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
                }

                otpService.deleteOtp(otpExpected.getId());
                GenericResponse response = new GenericResponse("Login successful 🎉");

                return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
        }

}
