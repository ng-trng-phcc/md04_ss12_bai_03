package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.ProductDTO;
import com.example.inventoryservice.dto.PurchaseRequest;
import com.example.inventoryservice.event.OrderCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tru kho qua endpoint purchase nguyen tu (Mockito, khong can broker). */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock RestTemplate restTemplate;

    @InjectMocks InventoryService service;

    @BeforeEach
    void injectBaseUrl() {
        ReflectionTestUtils.setField(service, "productServiceBaseUrl", "http://localhost:8082");
    }

    private OrderCreatedEvent event() {
        return OrderCreatedEvent.builder()
                .orderId(1L).customerId(7L).productId(3L).quantity(2).totalAmount(20000L).build();
    }

    @Test
    void deductStock_postsPurchaseWithQuantity() {
        when(restTemplate.postForObject(anyString(), any(PurchaseRequest.class), eq(ProductDTO.class)))
                .thenReturn(ProductDTO.builder().id(3L).name("P").price(10000L).stockQuantity(8).build());

        service.deductStock(event());

        ArgumentCaptor<PurchaseRequest> captor = ArgumentCaptor.forClass(PurchaseRequest.class);
        verify(restTemplate).postForObject(
                eq("http://localhost:8082/api/v1/products/3/purchase"), captor.capture(), eq(ProductDTO.class));
        assertThat(captor.getValue().getQuantity()).isEqualTo(2);
    }

    @Test
    void deductStock_outOfStock409_doesNotThrow() {
        when(restTemplate.postForObject(anyString(), any(PurchaseRequest.class), eq(ProductDTO.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.CONFLICT, "Conflict", null, null, null));

        assertThatNoException().isThrownBy(() -> service.deductStock(event()));
    }

    @Test
    void deductStock_serviceDown_throwsForRetry() {
        when(restTemplate.postForObject(anyString(), any(PurchaseRequest.class), eq(ProductDTO.class)))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("down"));

        assertThatThrownBy(() -> service.deductStock(event()))
                .isInstanceOf(IllegalStateException.class);
    }
}
