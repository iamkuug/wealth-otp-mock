package com.wealth.demo.repository;

import com.wealth.demo.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Otp findByAccountPhoneNumber(String phoneNumber);

    void deleteByAccountId(UUID accountId);
}