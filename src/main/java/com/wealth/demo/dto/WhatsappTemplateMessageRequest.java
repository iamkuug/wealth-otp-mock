package com.wealth.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class WhatsappTemplateMessageRequest {
    private String messaging_product;
    private String recipient_type;
    private String to;
    private String type;
    private Template template;

    @Getter
    @Setter
    @Builder
    public static class Template {
        private String name;
        private Language language;
        private List<Component> components;

        @Getter
        @Setter
        @Builder
        public static class Language {
            private String code;
        }

        @Getter
        @Setter
        @Builder
        public static class Component {
            private String type;
            private List<Parameter> parameters;
            private String sub_type;
            private int index;

            @Getter
            @Setter
            @Builder
            public static class Parameter {
                private String type;
                private String text;
            }
        }
    }
}