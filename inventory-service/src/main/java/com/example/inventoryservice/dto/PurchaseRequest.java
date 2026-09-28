package com.example.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Body cho POST /api/v1/products/{id}/purchase cua product-service. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequest {
    private Integer quantity;
}
