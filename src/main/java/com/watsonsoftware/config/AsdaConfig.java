package com.watsonsoftware.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AsdaConfig {
    private boolean on;
    private int rate;
    private String url;
    private String postcode;
    private String accountId;
    private boolean autoPlaceOrder;
    private String slotDate;
}
