package com.icsecurities.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendRequest {
    String phoneNumber;
    String otpCode;
}
