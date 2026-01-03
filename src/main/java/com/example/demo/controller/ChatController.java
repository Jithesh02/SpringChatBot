package com.example.demo.controller;


import com.example.demo.dto.ChatMessageDto;
import com.example.demo.service.chat.IConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}")
public class ChatController {

    private final IConversationService conversationService;

    @PostMapping ("/chat")
    public ResponseEntity<String> handleChatMessage(@RequestBody ChatMessageDto message)
    {
        String response = conversationService.handleChatMessage(message);
        return ResponseEntity.ok(response);
    }
}
