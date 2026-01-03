package com.example.demo.service.chat;

import com.example.demo.dto.ChatMessageDto;

public interface IConversationService {
    String handleChatMessage(ChatMessageDto message);
}
