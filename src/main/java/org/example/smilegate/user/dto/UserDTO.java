package org.example.smilegate.user.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.smilegate.user.domain.UserRole;

public class UserDTO {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserSignupRequest{
        private String username;
        @Column(nullable = false)
        private String email;
        @Column(nullable = false)
        private String password;
        private UserRole role;


    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserLoginResponse{
        Long user_id;
        @Column(nullable = false)
        private String email;
        @Column(nullable = false)
        private String password;
        private String accesstoken;
        private UserRole role;


    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserLoginRequest{
        @Column(nullable = false)
        private String email;
        @Column(nullable = false)
        private String password;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class VerificationRequest{
        @Column(nullable = false)
        private String email;

        private String code;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class SNSloginRequest{
        private String state;
        private String code;

    }


}
