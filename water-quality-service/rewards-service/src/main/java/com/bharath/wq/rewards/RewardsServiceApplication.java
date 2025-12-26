package com.bharath.wq.rewards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RewardsServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(RewardsServiceApplication.class, args);
  }
}
