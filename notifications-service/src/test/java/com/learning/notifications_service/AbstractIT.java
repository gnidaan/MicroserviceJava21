package com.learning.notifications_service;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.learning.notifications_service.domain.NotificationService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import({TestcontainersConfiguration.class})
public abstract class AbstractIT {
    @MockitoBean
    protected NotificationService notificationService;
}
