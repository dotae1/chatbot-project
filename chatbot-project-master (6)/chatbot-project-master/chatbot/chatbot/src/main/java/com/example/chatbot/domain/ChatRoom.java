package com.example.chatbot.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Document(collection = "chat_rooms")
public class ChatRoom {
    @Id
    private String objectId;
    private String userId;
    private String chatTitle;
    private List<ContextUser> contextUser = new ArrayList<>();
    private boolean deleteYn;

    // 기본 생성자 정의
    public ChatRoom() {
        this.chatTitle = "New chat";
        this.contextUser = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    public static class ContextUser {
        private String question;
        private String answer;
        private boolean isClova;
        private boolean modifyRequest;
        private String modifyText;

        // 기본 생성자 정의
        public ContextUser() {
        }
    }
}
