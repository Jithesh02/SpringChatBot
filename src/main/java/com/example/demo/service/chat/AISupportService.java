package com.example.demo.service.chat;

import com.example.demo.dto.ChatEntry;
import com.example.demo.utils.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AISupportService {

    private final ChatClient chatClient;

    public Mono<String> chatwithHistory(List<ChatEntry> history)
    {
        List<Message> messages = new ArrayList<>();

        messages.add(new SystemMessage(PromptTemplate.SUPPORT_PROMPT_TEMPLATE));

        for(ChatEntry chatEntry: history)
        {
            String role = chatEntry.getRole();
            String content = chatEntry.getContent();

            switch (role)
            {
                case "user":
                    messages.add(new UserMessage(content));
                    break;
                case "assistant":
                    messages.add(new AssistantMessage(content));
                    break;
                default:{

                }
            }
        }


        return Mono.fromCallable(()-> {
            ChatClient.CallResponseSpec responseSpec =chatClient
                    .prompt()
                    .messages(messages)
                    .options(ChatOptions
                            .builder()
                            .model("gpt-4o")
                            .temperature(0.7)
                            .maxTokens(200)
                            .build()
                    )
                    .call();
            String content = responseSpec.content();
            if(content == null)
            {
                throw new IllegalAccessException("No content available");
            }
            return content;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> generateUserConfirmationMessage()
    {
        String prompt = String.format(PromptTemplate.CUSTOMER_CONFIRMATION_PROMPT);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(prompt));



        return Mono.fromCallable(()-> {
            ChatClient.CallResponseSpec responseSpec =chatClient
                    .prompt()
                    .messages(messages)
                    .call();
            String content = responseSpec.content();
            if(content == null)
            {
                throw new IllegalAccessException("AI response is null");
            }
            return content;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    public Mono<String> summarizeUserConversation(String userConversationText)
    {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(PromptTemplate.CUSTOMER_CONVERSATION_SUMMARY_PROMPT));
        messages.add(new UserMessage(userConversationText));
        return Mono.fromCallable(()->
        {
            ChatClient.CallResponseSpec responseSpec = chatClient.prompt()
                    .messages(messages)
                    .call();
            String content = responseSpec.content();
            if(content == null)
            {
                throw new IllegalStateException("AI response content is null");
            }
            return content.trim();
        }).subscribeOn(Schedulers.boundedElastic());

    }

    public Mono<String> generateConversationTitle(String summarizedConversation)
    {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(PromptTemplate.TITLE_GENERATION_PROMPT));
        messages.add(new SystemMessage(summarizedConversation));

        return Mono.fromCallable(()->{
            ChatClient.CallResponseSpec responseSpec = chatClient.prompt()
                    .messages(messages)
                    .call();
            String content = responseSpec.content();
            if(content == null)
            {
                throw new IllegalStateException("AI response content is null");
            }
            return content.trim();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> generateEmailNotificationMessage() {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(PromptTemplate.EMAIL_NOTIFICATION_PROMPT));

        return Mono.fromCallable(()->{
            ChatClient.CallResponseSpec responseSpec = chatClient.prompt()
                    .messages(messages)
                    .call();
            String content = responseSpec.content();
            if(content==null)
            {
                throw new IllegalStateException("AI response content is null");
            }
            return content;
        }).subscribeOn(Schedulers.boundedElastic());
    }

}
