package com.sba.payment.client;

import com.sba.common.dto.OrderDto;
import com.sba.common.dto.UpdateOrderStatusRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class OrderClient {

    private final RestTemplate restTemplate;
    private final String orderServiceBaseUrl;

    public OrderClient(RestTemplate restTemplate,
                       @Value("${clients.order-service.base-url:http://localhost:8083}") String orderServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.orderServiceBaseUrl = orderServiceBaseUrl;
    }

    @Retryable(retryFor = RestClientException.class,
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${retry.delay-ms:300}", multiplier = 2.0))
    public OrderDto updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, String bearerToken) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", bearerToken);
        org.springframework.http.HttpEntity<UpdateOrderStatusRequest> httpEntity = new org.springframework.http.HttpEntity<>(request, headers);
        var response = restTemplate.exchange(orderServiceBaseUrl + "/orders/" + orderId + "/status",
                org.springframework.http.HttpMethod.PUT, httpEntity, OrderDto.class);
        return response.getBody();
    }
}
