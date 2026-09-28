package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.ProductDTO;
import com.example.inventoryservice.dto.PurchaseRequest;
import com.example.inventoryservice.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Tru kho bat dong bo qua endpoint purchase NGUYEN TU cua product-service
 * (khoa phan tan lock:product:{id} ben trong) -> khong race nhu GET+PUT.
 * Het hang (409) -> log va ack (khong retry vo han); loi mang/5xx -> throw de Kafka retry.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final RestTemplate restTemplate;

    @Value("${product-service.base-url:http://localhost:8082}")
    private String productServiceBaseUrl;

    public void deductStock(OrderCreatedEvent event) {
        String url = productServiceBaseUrl + "/api/v1/products/" + event.getProductId() + "/purchase";
        try {
            ProductDTO updated = restTemplate.postForObject(
                    url, new PurchaseRequest(event.getQuantity()), ProductDTO.class);
            log.info("Da tru kho productId={} con lai {} (orderId={})",
                    event.getProductId(),
                    updated != null ? updated.getStockQuantity() : "?",
                    event.getOrderId());
        } catch (HttpClientErrorException.Conflict e) {
            // Het hang: don nay khong the fulfill -> log va ack, khong retry
            log.warn("Het hang productId={} orderId={} -> bo qua (khong retry): {}",
                    event.getProductId(), event.getOrderId(), e.getMessage());
        } catch (RestClientException e) {
            log.warn("product-service khong kha dung khi tru kho productId={} orderId={}: {}",
                    event.getProductId(), event.getOrderId(), e.getMessage());
            throw new IllegalStateException("deduct stock failed, retry later", e);
        }
    }
}
