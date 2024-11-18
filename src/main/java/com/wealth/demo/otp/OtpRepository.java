package com.wealth.demo.otp;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<Otp, OtpId> {
}