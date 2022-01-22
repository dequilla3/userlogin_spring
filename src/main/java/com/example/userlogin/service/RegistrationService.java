package com.example.userlogin.service;

import com.example.userlogin.entity.AppUser;
import com.example.userlogin.entity.AppUserRole;
import com.example.userlogin.entity.ConfirmationToken;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {
    private final AppUserService appUserService;
    private final EmailValidatorService emailValidatorService;
    private final ConfirmationTokenService confirmationTokenService;

    public String register(RegistrationRequest registrationRequest) {
        boolean isValidEmail = emailValidatorService.test(registrationRequest.getEmail());
        if (!isValidEmail) {
            throw new RuntimeException("Email is not valid!");
        }
        return appUserService.signUpUser(createNewUserFromRequest(registrationRequest));
    }

    private AppUser createNewUserFromRequest(RegistrationRequest request) {
        AppUser appUser = new AppUser();
        appUser.setFirstName(request.getFirstName());
        appUser.setLastName(request.getLastName());
        appUser.setEmail(request.getEmail());
        appUser.setPassword(request.getPassword());
        appUser.setAppUserRole(AppUserRole.USER);
        return appUser;
    }

    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token)
                .orElseThrow(() -> new IllegalStateException("token not found"));

        LocalDateTime expiredAt = confirmationToken.getDateExpired();

        if (confirmationToken.getDateConfirmed() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        appUserService.enableAppUser(confirmationToken.getAppUser().getEmail());
        return "confirmed";
    }
}
