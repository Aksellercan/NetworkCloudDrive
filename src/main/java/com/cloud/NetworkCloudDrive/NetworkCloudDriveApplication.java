package com.cloud.NetworkCloudDrive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NetworkCloudDriveApplication {
    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(NetworkCloudDriveApplication.class);
        logger.info("Operating System: {}", System.getProperty("os.name"));
        logger.info("Operating System Version: {}", System.getProperty("os.version"));
        logger.info("Operating System Architecture: {}", System.getProperty("os.arch"));
        SpringApplication.run(NetworkCloudDriveApplication.class, args);
    }
}
