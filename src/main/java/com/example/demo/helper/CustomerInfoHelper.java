package com.example.demo.helper;

import com.example.demo.dto.ChatEntry;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

@Getter
@Setter
public class CustomerInfoHelper {

    private String phoneNumber;
    private String emailAddress;

    public static CustomerInfo extractUserInfoFromChatHistory(List<ChatEntry> history)
    {
        if (history == null || history.isEmpty())
        {
            return new CustomerInfo(null,null);
        }

        Optional<String> emailAddress = history.stream()
                .filter(chatEntry -> "user".equalsIgnoreCase(chatEntry.getRole()))
                .map(ChatEntry ::getContent )
                .filter(content->content!=null && !content.isBlank())
                .map(CustomerInfoHelper::extractEmail)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        Optional<String> phonenumber = history.stream()
                .filter(chatEntry -> "user".equalsIgnoreCase(chatEntry.getRole()))
                .map(ChatEntry ::getContent )
                .filter(content->content!=null && !content.isBlank())
                .map(CustomerInfoHelper::extractPhoneNumber)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        return new CustomerInfo(emailAddress.orElse(null),phonenumber.orElse(null));
    }

    private static Optional<String> extractEmail(String emailText)
    {
        return Optional.ofNullable(emailText)
                .filter(s->!s.isEmpty())
                .flatMap(t->{
                    Matcher matcher = RegexPattern.EMAIL_PATTERN.matcher(t);
                    return matcher.find() ? Optional.of(matcher.group()) : Optional.empty();
                });
    }

    private static Optional<String> extractPhoneNumber(String phoneNumber)
    {
        return Optional.ofNullable(phoneNumber)
                .filter(s->!s.isEmpty())
                .flatMap(t->{
                    Matcher matcher = RegexPattern.PHONE_PATTERN.matcher(t);
                    return matcher.find()?Optional.of(matcher.group()) : Optional.empty();
                });
    }
}
