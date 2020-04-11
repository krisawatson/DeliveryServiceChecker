package com.watsonsoftware.slack;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@Builder(builderClassName = "Builder")
@Data
public class SlackMessage implements Serializable {

    private String channel;
    private String type;
    private String username;
    private String text;
    private String icon_emoji;
}
