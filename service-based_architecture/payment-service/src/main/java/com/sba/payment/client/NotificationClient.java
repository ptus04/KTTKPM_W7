package com.sba.payment.client;

import com.sba.common.dto.NotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final String notificationServiceBaseUrl;

    public NotificationClient(RestTemplate restTemplate,
                              @Value("${clients.notification-service.base-url:http://localhost:8085}") String notificationServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
    }

    @Retryable(retryFor = RestClientException.class,
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${retry.delay-ms:300}", multiplier = 2.0))
    public void sendNotification(NotificationRequest request, String bearerToken) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", bearerToken);
        org.springframework.http.HttpEntity<NotificationRequest> httpEntity = new org.springframework.http.HttpEntity<>(request, headers);
        restTemplate.exchange(notificationServiceBaseUrl + "/notifications",
                org.springframework.http.HttpMethod.POST, httpEntity, Void.class);
    }
}
