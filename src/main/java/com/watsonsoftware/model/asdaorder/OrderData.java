package com.watsonsoftware.model.asdaorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderData {
    @JsonProperty(value = "service_info")
    private ServiceInfo serviceInfo;

    @JsonProperty(value = "start_date")
    private String startDate;

    @JsonProperty(value = "end_date")
    private String endDate;

    @JsonProperty(value = "reserved_slot_id")
    private String reservedSlotId;

    @JsonProperty(value = "service_address")
    private ServiceAddress serviceAddress;

    @JsonProperty(value = "customer_info")
    private CustomerInfo customerInfo;

    @JsonProperty(value = "order_info")
    private OrderInfo orderInfo;
}
