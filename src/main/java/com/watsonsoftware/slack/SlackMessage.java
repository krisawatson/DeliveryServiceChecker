package com.watsonsoftware.slack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@Builder(builderClassName = "Builder")
@Getter
@Setter
public class SlackMessage implements Serializable {

    private String channel;
    private String type;
    private String username;
    private String text;
    private String icon_emoji;
}
