package com.watsonsoftware.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Configuration {
    private SlackConfig slack;
    private AsdaConfig asda;
    private IcelandConfig iceland;
}
