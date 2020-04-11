package com.watsonsoftware.model.asdaorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderInfo {

    @JsonProperty(value = "order_id")
    private String orderId;

    @JsonProperty(value = "restricted_item_types")
    private List<String> restrictedItemTypes;

    private double volume;

    private double weight;

    @JsonProperty(value = "sub_total_amount")
    private double subTotalAmount;

    @JsonProperty(value = "line_item_count")
    private int lineItemCount;

    @JsonProperty(value = "total_quantity")
    private int totalQuantity;
}
