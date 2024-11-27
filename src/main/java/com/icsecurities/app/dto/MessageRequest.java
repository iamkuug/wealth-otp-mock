package com.icsecurities.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    public String phoneNumber;
    public String messageBody;
}
