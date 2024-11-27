package com.icsecurities.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyRequest {

    private String token;
    private String otpCode;
}