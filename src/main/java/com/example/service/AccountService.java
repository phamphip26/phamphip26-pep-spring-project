package com.example.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.entity.Account;
import com.example.exception.AuthenticationException;
import com.example.exception.DuplicateUsernameException;
import com.example.exception.ValidationException;
import com.example.repository.AccountRepository;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public Account register(Account account) throws DuplicateUsernameException, ValidationException {
        if (null == account.getUsername() || account.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username cannot be blank");
        }

        if (null == account.getPassword() || 4 > account.getPassword().length()) {
            throw new ValidationException("Password must be at least 4 characters long");
        }

        Optional<Account> existingAccount = accountRepository.findByUsername(account.getUsername());

        if (existingAccount.isPresent()) {
            throw new DuplicateUsernameException("Username already exists");
        }

        return accountRepository.save(account);
    }

    public Account authenticate(String username, String password) throws AuthenticationException {
        Optional<Account> account = accountRepository.findByUsername(username);
        
        if (account.isPresent()) {
            Account existAccount = account.get();

            if (existAccount.getPassword().equals(password)) {
                return existAccount;
            }
        }
        
        throw new AuthenticationException("Invalid username or password");
    }
}
