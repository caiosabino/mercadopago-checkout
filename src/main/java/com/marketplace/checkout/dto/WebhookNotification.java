package com.marketplace.checkout.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebhookNotification {

    private String id;
    private String action;
    private String apiVersion;

    @JsonProperty("live_mode")
    private Boolean liveMode;

    private String type;
    private String dateCreated;
    private Long userId;

    private DataInfo data;

    @Data
    public static class DataInfo {
        private String id;
    }
}
