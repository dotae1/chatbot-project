package com.example.chatbot.controller;

import com.example.chatbot.domain.ChatRoom;
import com.example.chatbot.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat_rooms")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChatRoom chatRoom){
        ChatRoom createdChatRoom = chatRoomService.createChatRoom(chatRoom);
        return ResponseEntity.ok(createdChatRoom);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatRoom> getChatRoomById(@PathVariable String id){
        return chatRoomService.getChatRoomById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllChatRooms(){
        List<ChatRoom> chatRooms = chatRoomService.getAllChatRoom();
        return ResponseEntity.ok(chatRooms);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChatRoom> updateChatRoom(@PathVariable String id, @RequestBody ChatRoom updatedChatRoom){
        ChatRoom updatedChatRoomEntity = chatRoomService.updateChatRoom(id, updatedChatRoom);
        return ResponseEntity.ok(updatedChatRoomEntity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable String id){
        chatRoomService.deleteChatRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/contextUsers")
    public ResponseEntity<List<ChatRoom.ContextUser>> getChatRoomContextUsers(@PathVariable String id) {
        return chatRoomService.getChatRoomById(id)
                .map(chatRoom -> ResponseEntity.ok(chatRoom.getContextUser()))
                .orElse(ResponseEntity.notFound().build());
    }
}
