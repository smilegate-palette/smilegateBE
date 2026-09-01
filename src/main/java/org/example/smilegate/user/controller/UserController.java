package org.example.smilegate.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.smilegate.config.global.Oauth.OAuthService;
import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.dto.UserDTO;
import org.example.smilegate.user.service.EmailVerficationService;
import org.example.smilegate.user.service.OauthService.OAuthServiceFactory;
import org.example.smilegate.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class UserController {
    private final UserService userService;
    private final EmailVerficationService emailVerficationService;
    private final OAuthServiceFactory oAuthServiceFactory;


    @PostMapping(value = "/login", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public UserDTO.UserLoginResponse login(@RequestBody UserDTO.UserLoginRequest request) throws IllegalAccessException {
        try {
            UserDTO.UserLoginResponse response = userService.login(request);
            return response;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value="/signup", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> Signup(@RequestBody UserDTO.UserSignupRequest request){
        try{
            if(!userService.Signup(request)) return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("이메일 인증을 해주세요.");
            else { return ResponseEntity.status(HttpStatus.OK).body("회원가입에 성공했습니다.");}
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입에 실패했습니다." + e);
        }
    }

    @PostMapping(value = "/signup/sendcode", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> VerificationCodeSend(@RequestBody UserDTO.VerificationRequest request) {
        try {
            emailVerficationService.SendVerificationCode(request.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body("인증번호 전송에 성공했습니다.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping(value = "/signup/verification", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> EmailVerification(@RequestBody UserDTO.VerificationRequest request) {
        try {
            emailVerficationService.verify(request.getEmail(), request.getCode());
            return ResponseEntity.status(HttpStatus.OK).body("이메일 인증에 성공했습니다.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping(value = "/{provider}/callback")
    public ResponseEntity<?> oauthCallback(@PathVariable String provider, @RequestBody UserDTO.SNSloginRequest loginRequest) {
        OAuthService oAuthService = oAuthServiceFactory.getService(provider);

        String accessToken = oAuthService.getAccessToken(loginRequest);
        User user = oAuthService.getUserInfo(accessToken);

        UserDTO.UserLoginResponse response = userService.SNSLogin(user, provider);
        return ResponseEntity.ok(response);
    }



}
