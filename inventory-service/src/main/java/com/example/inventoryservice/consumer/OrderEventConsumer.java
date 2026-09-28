package com.example.inventoryservice.consumer;

import com.example.inventoryservice.event.OrderCreatedEvent;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Cung lang nghe topic order-events nhung group-id = inventory-group (khac email-group)
 * -> 1 event kich hoat dong thoi ca tru kho va gui mail, doc lap nhau.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "order-events",
            groupId = "inventory-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(
            @Payload OrderCreatedEvent event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("inventory-service nhan su kien: orderId={} productId={} quantity={} | key={} partition={} offset={} groupId=inventory-group",
                event.getOrderId(), event.getProductId(), event.getQuantity(), key, partition, offset);
        try {
            inventoryService.deductStock(event);
        } catch (Exception e) {
            log.error("inventory-service xu ly that bai orderId={}: {}", event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
}
