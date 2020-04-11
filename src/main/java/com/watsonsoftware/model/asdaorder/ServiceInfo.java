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
public class ServiceInfo {

    @JsonProperty(value = "fulfillment_type", defaultValue = "DELIVERY")
    private String fulfillmentType;

    @JsonProperty(value = "enable_express")
    private boolean enableExpress;
}
