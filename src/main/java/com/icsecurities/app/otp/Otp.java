package com.icsecurities.app.otp;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "otp")
@IdClass(OtpId.class)
public class Otp implements Serializable {
    @Id
    private String otpCode;
    @Id
    private String token;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    private String phoneNumber;

    public Otp(String phoneNumber, String otpCode, String token, Date expiryDate) {
        this.phoneNumber = phoneNumber;
        this.otpCode = otpCode;
        this.token = token;
        this.expiryDate = expiryDate;
    }
}