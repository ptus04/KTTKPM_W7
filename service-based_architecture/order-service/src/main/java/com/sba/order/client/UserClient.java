package com.sba.order.client;

import com.sba.common.dto.UserSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public UserClient(RestTemplate restTemplate,
                      @Value("${clients.user-service.base-url:http://localhost:8081}") String userServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @Retryable(retryFor = RestClientException.class,
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${retry.delay-ms:300}", multiplier = 2.0))
    public boolean userExists(Long userId, String bearerToken) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", bearerToken);
        org.springframework.http.HttpEntity<Void> request = new org.springframework.http.HttpEntity<>(headers);
        var response = restTemplate.exchange(userServiceBaseUrl + "/users",
                org.springframework.http.HttpMethod.GET, request, UserSummary[].class);
        List<UserSummary> users = Arrays.asList(response.getBody() == null ? new UserSummary[0] : response.getBody());
        return users.stream().anyMatch(u -> u.id().equals(userId));
    }
}
