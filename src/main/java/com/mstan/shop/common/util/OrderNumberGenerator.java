package com.mstan.shop.common.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class OrderNumberGenerator {

    public String generate() {
        String randomSuffix = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return "ORD-" + Instant.now().toEpochMilli() + "-" + randomSuffix;
    }
}
