package com.sba.order.client;

import com.sba.common.dto.FoodDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class FoodClient {

    private final RestTemplate restTemplate;
    private final String foodServiceBaseUrl;

    public FoodClient(RestTemplate restTemplate,
                      @Value("${clients.food-service.base-url:http://localhost:8082}") String foodServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.foodServiceBaseUrl = foodServiceBaseUrl;
    }

    @Retryable(retryFor = RestClientException.class,
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${retry.delay-ms:300}", multiplier = 2.0))
    public FoodDto getFoodById(Long id, String bearerToken) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", bearerToken);
        org.springframework.http.HttpEntity<Void> request = new org.springframework.http.HttpEntity<>(headers);
        var response = restTemplate.exchange(foodServiceBaseUrl + "/foods/" + id,
                org.springframework.http.HttpMethod.GET, request, FoodDto.class);
        return response.getBody();
    }
}
