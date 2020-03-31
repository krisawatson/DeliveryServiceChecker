package com.watsonsoftware.model;

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
public class Slots {
    @JsonProperty("supported_timezone")
    String supportedTimezone;

    @JsonProperty("slot_info")
    SlotInfo slotInfo;
}
