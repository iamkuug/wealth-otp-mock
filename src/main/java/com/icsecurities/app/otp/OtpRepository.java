package com.icsecurities.app.otp;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface OtpRepository extends JpaRepository<Otp, OtpId> {

    @Query("SELECT o FROM Otp o WHERE o.token = :token AND o.otpCode = :otpCode ORDER BY o.createdAt DESC")
    Otp findMostRecentOtp(@Param("token") String token, @Param("otpCode") String otpCode);

    @Modifying
    @Transactional
    @Query("DELETE FROM Otp o WHERE o.expiryDate < :now")
    void deleteByExpiryDateBefore(Date now);

    List<Otp> findByPhoneNumber(String phoneNumber);
}