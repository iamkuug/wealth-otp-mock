package com.wealth.demo.msg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.wealth.demo.dto.GenericResponse;
import com.wealth.demo.dto.MessageRequest;
import com.wealth.demo.ex.BadRequestException;

@RestController
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/api/message/send")
    public GenericResponse sendMessage(@RequestBody MessageRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String messageBody = request.getMessageBody();

        System.out.println("phoneNumber: " + phoneNumber);

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new BadRequestException("Field `phoneNumber` is required");
        }

        if (!phoneNumber.matches("\\+?[0-9]+")) {
            throw new BadRequestException("Field `phoneNumber` has invalid format");
        }

        if (phoneNumber.length() != 12) {
            throw new BadRequestException("Field `phoneNumber` has invalid length. Ex: 233123456789");
        }

        if (messageBody == null || messageBody.isEmpty()) {
            throw new BadRequestException("Field `messageBody` is required");
        }

        messageService.send(phoneNumber, messageBody);

        return new GenericResponse("Message Sent");
    }
}
