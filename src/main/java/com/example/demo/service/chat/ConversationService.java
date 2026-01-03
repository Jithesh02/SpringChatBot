package com.example.demo.service.chat;

import com.example.demo.dto.ChatEntry;
import com.example.demo.dto.ChatMessageDto;
import com.example.demo.event.TicketCreationEvent;
import com.example.demo.helper.CustomerInfo;
import com.example.demo.helper.CustomerInfoHelper;
import com.example.demo.model.Converstion;
import com.example.demo.model.Customer;
import com.example.demo.model.Ticket;
import com.example.demo.repository.ConverstionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ticket.ITicketService;
import com.example.demo.websocket.WebConfigMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService {
    private final AISupportService aiSupportService;
    private final UserRepository userRepository;
    private final WebConfigMessageSender webConfigMessageSender;
    private  final ConverstionRepository converstionRepository;
    private final ITicketService iTicketService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Map<String, List<ChatEntry>> activeConversations = new ConcurrentHashMap<>();


    @Override
    public String handleChatMessage(ChatMessageDto chatMessage) {
        String sessionId = chatMessage.getSessionId();
        String useMessage = chatMessage.getMessage() != null ? chatMessage.getMessage().trim() : "";
        log.info("The message session Id : {} ", sessionId);
        log.info("The message content : {} ", useMessage);

        List<ChatEntry> history = activeConversations.computeIfAbsent(sessionId,
                k -> Collections.synchronizedList(new ArrayList<>()));
        history.add(new ChatEntry("user", useMessage));

        String aiResponseText;
        try {
            aiResponseText = aiSupportService.chatwithHistory(history).block();
            log.info("The message content : {} ", aiResponseText);
        } catch (Exception e) {
            aiResponseText = "Sorry, I'm having trouble processing your request right now.";
        }
        if (aiResponseText == null) {
            return "";
        }
        history.add(new ChatEntry("assistant", aiResponseText));

        if (aiResponseText.contains("TICKET_CREATION_READY")) {
            try {
                String confirmationMessage = aiSupportService.generateUserConfirmationMessage().block();
                log.info("confirmation message"+ confirmationMessage+sessionId);
                history.add(new ChatEntry("assistant", confirmationMessage));
                CompletableFuture.runAsync(()->{
                    try{
                        Ticket tempTicket = finalizeConversationAndCreateTicket(sessionId);
                        if(tempTicket != null)
                        {
                            history.add(new ChatEntry("system","The email notification has been sent"));

                            String feedbackMessage = aiSupportService.generateEmailNotificationMessage().block();

                            if(feedbackMessage != null)
                            {
                                List<ChatEntry> currentHistory = activeConversations.get(sessionId);
                                if(currentHistory != null)
                                {
                                    currentHistory.add(new ChatEntry("assistant",feedbackMessage));
                                }
                                webConfigMessageSender.sendMessageToUser(sessionId,feedbackMessage);
                            }
                        }
                    }
                    catch(Exception e)
                    {
                        //log any error here
                    }
                });
                Ticket tempTicket = finalizeConversationAndCreateTicket(sessionId);
                return confirmationMessage;
            } catch (Exception e) {
                return "Error Generating confirmation message";
            }
        }

        return aiResponseText;
    }

    private Customer getCustomerInformation(List<ChatEntry> history) {
        CustomerInfo customerInfo = CustomerInfoHelper.extractUserInfoFromChatHistory(history);
        log.info("email={}, phone={}", customerInfo.emailAddress(), customerInfo.phoneNumber());
        return userRepository.findByEmailAddressAndPhoneNumber(customerInfo.emailAddress(),customerInfo.phoneNumber()
                );
    }

    private Ticket finalizeConversationAndCreateTicket(String sessionId) {
        List<ChatEntry> history = activeConversations.get(sessionId);
        log.info("History : {}", history);
        Customer customer = getCustomerInformation(history);
        log.info("This is the customer information : {}", customer.getId());
        if (customer == null) {
            String errorMessage = "Sorry, The email or phone number you provided does no exist in our database." +
                    "Please provide your registered email and phone number to continue.";
            webConfigMessageSender.sendMessageToUser(sessionId,errorMessage);
            if (history != null) {
                history.add(new ChatEntry("system", errorMessage));
            }
            //
            return null;
        }

        Converstion converstion = getConversation(customer);
        try{
            List<ChatEntry> userConversation = history.stream()
                    .filter(chatEntry -> "user".equals(chatEntry.getRole()))
                    .toList();

            String conversationSummary = aiSupportService
                    .summarizeUserConversation(
                            userConversation
                                    .toString())
                    .block();

            String conversationTitle = aiSupportService.generateConversationTitle(conversationSummary).block();

            converstion.setConversationTitle(conversationTitle !=null ? conversationTitle.trim():"Untitled Conversation");

            converstion.setConversationTitle(conversationTitle);
            converstion.setConversationSummary(conversationSummary);

            Converstion savedConversation = converstionRepository.save(converstion);

            Ticket savedTicket = iTicketService.createTicketForConversation(converstion);

            savedConversation.setTicket(savedTicket);
            savedConversation.setTicketCreated(true);
            converstionRepository.save(savedConversation);
            //send email confirmation to customer
            applicationEventPublisher.publishEvent(new TicketCreationEvent(savedTicket));
            //remove the customer conversation from the memory
            activeConversations.remove(sessionId);
            return savedTicket;
        }
        catch(Exception e)
        {
            String errorMsg = "Error occured during conversation creation."+e.getMessage();
            webConfigMessageSender.sendMessageToUser(sessionId,errorMsg);
            return null;
        }

        //
    }

    private static Converstion getConversation(Customer customer)
    {
        Converstion converstion = new Converstion();
        converstion.setCustomer(customer);
        converstion.setTicketCreated(false);
        return converstion;
    }
}
