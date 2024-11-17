package com.example.chatbot.repository;

import com.example.chatbot.domain.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    boolean existsByChatTitle(String chatTitle);
}
