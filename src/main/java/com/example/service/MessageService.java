package com.example.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.entity.Account;
import com.example.entity.Message;
import com.example.exception.MessageCreationException;
import com.example.exception.UpdateMessageException;
import com.example.repository.AccountRepository;
import com.example.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AccountRepository accountRepository;

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Message getMessageById(Integer messageId) {
        Optional<Message> message = messageRepository.findById(messageId);

        return message.orElse(null);
    }

    public List<Message> getAllMessagesByUserId(Integer userId) {
        return messageRepository.findAllByPostedBy(userId);
    }

    public Message createMessage(String messageText, Integer postedBy, Long time) throws MessageCreationException {
        if (null == messageText || messageText.trim().isEmpty()) {
            throw new MessageCreationException("Message text cannot be blank");
        }

        if (254 < messageText.length()) {
            throw new MessageCreationException("Message text must be under 255 characters");
        }
        
        Optional<Account> account = accountRepository.findById(postedBy);

        if (!account.isPresent()) {
            throw new MessageCreationException("User does not exist");
        }

        Message message = new Message(postedBy, messageText, time);

        return messageRepository.save(message);
    }

    public int deleteMessageById(Integer messageId) {
        Optional<Message> message = messageRepository.findById(messageId);

        if (message.isPresent()) {
            messageRepository.deleteById(messageId);

            return 1;
        } else {
            return 0;
        }
    }

    public int updateMessage(Integer messageId, String jsonBody) throws UpdateMessageException, JsonMappingException, JsonProcessingException {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);

        if (!optionalMessage.isPresent()) {
            throw new UpdateMessageException("Message not found");
        }

        Message message = optionalMessage.get();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonBody);

        if (!jsonNode.has("messageText")) {
            throw new UpdateMessageException("Field messageText is not exist");
        }

        String newMessageText = jsonNode.get("messageText").asText();
        
        if (null == newMessageText || newMessageText.isBlank()) {
            throw new UpdateMessageException("Message text cannot be blank");
        }

        if (254 < newMessageText.length()) {
            throw new UpdateMessageException("Message text must not exceed 255 characters");
        }

        Integer postedBy = jsonNode.has("postedBy") ? jsonNode.get("messageText").asInt() : null;
        
        if (null != postedBy) {
            Optional<Account> account = accountRepository.findById(postedBy);

            if (!account.isPresent()) {
                throw new UpdateMessageException("User does not exist");
            }

            message.setPostedBy(postedBy);
        }

        Long timePostedEpoch = jsonNode.has("timePostedEpoch") ? jsonNode.get("timePostedEpoch").asLong() : null;
        
        if (null != timePostedEpoch) {
            message.setTimePostedEpoch(timePostedEpoch);
        }
    
        message.setMessageText(newMessageText);
        messageRepository.save(message);

        return 1;
    }
}
