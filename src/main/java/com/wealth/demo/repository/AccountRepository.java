package com.wealth.demo.repository;

import com.wealth.demo.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Account findByPhoneNumber(String phoneNumber);
}
