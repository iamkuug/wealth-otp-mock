
package com.icsecurities.app.otp;

import java.io.Serializable;

import lombok.Data;

@Data
public class OtpId implements Serializable {
    private String otpCode;
    private String token;

    public OtpId() {
    }

    public OtpId(String otpCode, String token) {
        this.otpCode = otpCode;
        this.token = token;
    }
}