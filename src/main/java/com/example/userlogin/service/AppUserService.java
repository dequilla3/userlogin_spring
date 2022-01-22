package com.example.userlogin.service;

import com.example.userlogin.entity.AppUser;
import com.example.userlogin.entity.ConfirmationToken;
import com.example.userlogin.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final static String USER_NOT_FOUND = "User with email %s not found";
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND, email)));
    }

    public String signUpUser(AppUser appUser) {
        boolean userExist = userRepository.findUserByEmail(appUser.getEmail()).isPresent();
        if (userExist) {
            throw new RuntimeException("email already exist!");
        }
        String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());
        appUser.setPassword(encodedPassword);

        userRepository.save(appUser);

        // TODO: Send confirmation token
        String token = UUID.randomUUID().toString();
        confirmationTokenService.saveConfirmationToken(createNewToken(token, appUser));
        return token;
    }

    private ConfirmationToken createNewToken(String token, AppUser appUser) {
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setToken(token);
        confirmationToken.setDateCreated(LocalDateTime.now());
        confirmationToken.setDateExpired(LocalDateTime.now().plusMinutes(15));
        confirmationToken.setAppUser(appUser);
        return confirmationToken;
    }

    public int enableAppUser(String email) {
        return userRepository.enableAppUser(email);
    }
}
