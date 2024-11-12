package com.wealth.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyRequest {

    private String phoneNumber;
    private String otp;
}