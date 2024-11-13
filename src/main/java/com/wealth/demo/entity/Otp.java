package com.wealth.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "otp", uniqueConstraints = @UniqueConstraint(columnNames = "account_id"))
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String otp;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    public Otp(String otp, Account account) {
        this.otp = otp;
        this.account = account;
    }
}
