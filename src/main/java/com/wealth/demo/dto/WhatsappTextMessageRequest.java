package com.wealth.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WhatsappTextMessageRequest {
    private String messaging_product;
    private String recipient_type;
    private String to;
    private String type;
    private Text text;

    @Getter
    @Setter
    @Builder
    public static class Text {
        private boolean preview_url;
        private String body;
    }
}
