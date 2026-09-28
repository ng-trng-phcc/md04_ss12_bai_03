package com.example.inventoryservice.consumer;

import com.example.inventoryservice.event.OrderCreatedEvent;
import com.example.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/** Consumer test (Mockito, khong can broker). */
@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock InventoryService inventoryService;

    @InjectMocks OrderEventConsumer consumer;

    @Test
    void listen_delegatesToInventoryService() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(1L).customerId(7L).productId(3L).quantity(2).totalAmount(20000L).build();

        consumer.listen(event, "1", 0, 0L);

        verify(inventoryService).deductStock(event);
    }
}
