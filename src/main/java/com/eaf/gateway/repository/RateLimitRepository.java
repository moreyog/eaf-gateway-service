package com.eaf.gateway.repository;

import com.eaf.gateway.entity.RateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface RateLimitRepository extends JpaRepository<RateLimit, String> {
    RateLimit findByUserIdAndIpAddressAndRequestDate(String userId, String ipAddress, Date requestDate);

    RateLimit findByIpAddressAndRequestDate(String ipAddress, Date requestDate);
}
