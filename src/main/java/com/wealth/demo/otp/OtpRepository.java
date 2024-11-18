package com.wealth.demo.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OtpRepository extends JpaRepository<Otp, OtpId> {

    @Query("SELECT o FROM Otp o WHERE o.token = :token AND o.otpCode = :otpCode ORDER BY o.createdAt DESC")
    Otp findMostRecentOtp(@Param("token") String token, @Param("otpCode") String otpCode);
}