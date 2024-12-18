package com.example.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.entity.Account;
import com.example.entity.Message;
import com.example.exception.AuthenticationException;
import com.example.exception.DuplicateUsernameException;
import com.example.exception.MessageCreationException;
import com.example.exception.UpdateMessageException;
import com.example.service.AccountService;
import com.example.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller using Spring. The endpoints you will need can be
 * found in readme.md as well as the test cases. You be required to use the @GET/POST/PUT/DELETE/etc Mapping annotations
 * where applicable as well as the @ResponseBody and @PathVariable annotations. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
@RestController
public class SocialMediaController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private MessageService messageService;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody Account account) {
        try {
            Account savedAccount = accountService.register(account);

            return ResponseEntity.ok(savedAccount);
        } catch (DuplicateUsernameException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Account account) {
        try {
            Account authenticatedAccount = accountService.authenticate(account.getUsername(), account.getPassword());
    
            return ResponseEntity.ok(authenticatedAccount);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/messages")
    public ResponseEntity createMessage(@RequestBody Message message) {
        try {
            Message createdMessage = messageService.createMessage(
                message.getMessageText(), 
                message.getPostedBy(), 
                message.getTimePostedEpoch()
            );
            
            return ResponseEntity.ok(createdMessage);
        } catch (MessageCreationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/messages")
    public ResponseEntity getAllMessages() {
        List<Message> messages = messageService.getAllMessages();

        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/{message_id}")
    public ResponseEntity getMessageById(@PathVariable("message_id") Integer messageId) {
        Message message = messageService.getMessageById(messageId);

        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/messages/{message_id}")
    public ResponseEntity deleteMessage(@PathVariable("message_id") Integer messageId) {
        int result = messageService.deleteMessageById(messageId);

        return ResponseEntity.ok(1 == result ? 1 : null);
    }

    @PatchMapping("/messages/{message_id}")
    public ResponseEntity updateMessage(@PathVariable("message_id") Integer messageId, @RequestBody String jsonBody) {
        try {
            int updated = messageService.updateMessage(messageId, jsonBody);

            return ResponseEntity.ok(updated);
        } catch (UpdateMessageException|JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/accounts/{account_id}/messages")
    public ResponseEntity getAllMessagesByAccountId(@PathVariable("account_id") Integer accountId) {
        List<Message> messages = messageService.getAllMessagesByUserId(accountId);

        return ResponseEntity.ok(messages);
    }
}
