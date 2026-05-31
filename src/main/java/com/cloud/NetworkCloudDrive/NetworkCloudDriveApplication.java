package com.cloud.NetworkCloudDrive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class NetworkCloudDriveApplication {
    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(NetworkCloudDriveApplication.class);
        logger.info("Operating System: {}", System.getProperty("os.name"));
        SpringApplication.run(NetworkCloudDriveApplication.class, args);
    }
}
