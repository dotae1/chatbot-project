package com.example.chatbot.service;

import com.example.chatbot.repository.ChatRoomRepository;
import com.example.chatbot.domain.ChatRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomService {
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    public ChatRoom createChatRoom(ChatRoom chatRoom){
        return chatRoomRepository.save(chatRoom);
    }

    public Optional<ChatRoom> getChatRoomById(String id){
        return chatRoomRepository.findById(id);
    }

    public List<ChatRoom> getAllChatRoom(){
        return chatRoomRepository.findAll();
    }

    public ChatRoom updateChatRoom(String id, ChatRoom updateChatRoom) {
        return chatRoomRepository.findById(id).map(existingChatRoom -> {
            existingChatRoom.setUserId(updateChatRoom.getUserId());
            existingChatRoom.setChatTitle(updateChatRoom.getChatTitle());
            existingChatRoom.setContextUser(updateChatRoom.getContextUser());
            existingChatRoom.setDeleteYn(updateChatRoom.isDeleteYn());
            return chatRoomRepository.save(existingChatRoom);
        }).orElseGet(() -> {
            updateChatRoom.setObjectId(id);
            return chatRoomRepository.save(updateChatRoom);
        });
    }

    public void deleteChatRoom(String id){
        chatRoomRepository.deleteById(id);
    }

    public void addMessageToChatRoom(String chatRoomId, ChatRoom.ContextUser message) {
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(chatRoomId);
        if (optionalChatRoom.isPresent()) {
            ChatRoom chatRoom = optionalChatRoom.get();
            chatRoom.getContextUser().add(message);
            chatRoomRepository.save(chatRoom);
        } else {
            throw new IllegalArgumentException("ChatRoom with id " + chatRoomId + " not found.");
        }
    }
}
