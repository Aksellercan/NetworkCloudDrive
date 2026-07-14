package com.cloud.NetworkCloudDrive;

import com.cloud.NetworkCloudDrive.Services.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.TestPropertySource;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:/application-test.properties")
class AsyncConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void applicationTaskExecutorBeanExists() {
        boolean hasExecutor = applicationContext.containsBean("applicationTaskExecutor")
                || applicationContext.containsBean("taskExecutor");
        if (!hasExecutor) {
            // Log available bean names for debugging
            String[] beanNames = applicationContext.getBeanDefinitionNames();
            boolean foundAnyExecutor = false;
            for (String name : beanNames) {
                if (name.toLowerCase().contains("executor")
                        || name.toLowerCase().contains("task")
                        || name.toLowerCase().contains("async")) {
                    foundAnyExecutor = true;
                    break;
                }
            }
        }
        assertTrue(hasExecutor,
                "Expected applicationTaskExecutor or taskExecutor bean from TaskExecutionAutoConfiguration");
    }

    @Test
    void asyncAnnotationOnStoreFile() throws Exception {
        java.lang.reflect.Method method = FileService.class.getMethod(
                "storeFile", InputStream.class, String.class, String.class);
        assertNotNull(method.getAnnotation(Async.class));
    }

    @Test
    void asyncAnnotationOnGetFile() throws Exception {
        java.lang.reflect.Method method = FileService.class.getMethod(
                "getFile", com.cloud.NetworkCloudDrive.Models.FileMetadata.class, String.class);
        assertNotNull(method.getAnnotation(Async.class));
    }
}
