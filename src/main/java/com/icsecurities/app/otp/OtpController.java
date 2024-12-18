package com.icsecurities.app.otp;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.icsecurities.app.dto.SendRequest;
import com.icsecurities.app.dto.SendResponse;
import com.icsecurities.app.dto.GenericResponse;
import com.icsecurities.app.dto.VerifyRequest;
import com.icsecurities.app.ex.BadRequestException;
import com.icsecurities.app.ex.GoneRequestException;
import com.icsecurities.app.ex.UnauthorizedRequestException;

@RestController
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/api/otp/send")
    public SendResponse generateOTP(
            @RequestBody SendRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String otpCode = request.getOtpCode();

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new BadRequestException("Field `phoneNumber` is required");
        }

        if (otpCode == null || otpCode.isEmpty()) {
            throw new BadRequestException("Field `otpCode` is required");
        }

        if (!phoneNumber.matches("\\+?[0-9]+")) {
            throw new BadRequestException("Field `phoneNumber` has invalid format");
        }

        if (phoneNumber.length() != 12) {
            throw new BadRequestException("Field `phoneNumber` has invalid length. Ex: 233123456789");
        }

        if (!otpCode.matches("\\d+")) {
            throw new BadRequestException("Field `otpCode` must contain only numeric values. Ex: 1234");
        }

        // if (!otpService.isPhoneNumberRegisteredOnWhatsapp(phoneNumber)) {
        // throw new BadRequestException("Phone number provided is not registered on
        // whatsapp");
        // }

        // purge all expired otps
        otpService.purgeAllExpiredOtps(phoneNumber);

        String token = otpService.generateToken();
        Date expiryDate = otpService.generateExpiryDate(1);

        otpService.saveOtp(new Otp(phoneNumber, otpCode, token, expiryDate));

        otpService.sendOtp(phoneNumber, otpCode);

        return new SendResponse(token);

    }

    @PostMapping("/api/otp/verify")
    public GenericResponse verifyOTP(
            @RequestBody VerifyRequest request) {
        String otpCodeGot = request.getOtpCode();
        String token = request.getToken();

        if (otpCodeGot == null || otpCodeGot.isEmpty()) {
            throw new BadRequestException("Field `otpCode` is required");
        }

        if (token == null || token.isEmpty()) {
            throw new BadRequestException("Field `token` is required");
        }

        Otp otp = otpService.getOtp(token, otpCodeGot);

        // validate otp
        if (otp.getExpiryDate().before(new Date())) {
            otpService.deleteOtp(otp);
            throw new GoneRequestException("OTP has expired");
        }

        if (!otp.getOtpCode().equals(otpCodeGot)) {
            otpService.deleteOtp(otp);
            throw new UnauthorizedRequestException("OTP is invalid");
        }

        otpService.deleteOtp(otp);

        return new GenericResponse("OTP verified");
    }
}