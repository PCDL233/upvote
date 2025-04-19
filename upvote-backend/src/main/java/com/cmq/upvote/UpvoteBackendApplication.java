package com.cmq.upvote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UpvoteBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpvoteBackendApplication.class, args);
    }

}
