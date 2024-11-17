package com.example.chatbot.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChatMessage {
    private String message;
    private String chatRoomId;
}
