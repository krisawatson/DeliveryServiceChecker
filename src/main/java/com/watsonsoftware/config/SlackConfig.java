package com.watsonsoftware.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlackConfig {
    private String channel;
    private String username;
    private List<String> notify;
    private String webhook;
}
