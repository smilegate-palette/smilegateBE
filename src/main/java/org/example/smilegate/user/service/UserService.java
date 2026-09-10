package org.example.smilegate.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.smilegate.config.global.Jwt.JwtUtil;
import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.domain.UserRole;
import org.example.smilegate.user.dto.UserDTO;
import org.example.smilegate.user.repository.UserRepository;
import org.example.smilegate.user.repository.VerifiacationCodeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerficationService emailVerficationService;
    private final VerifiacationCodeRepository verifiacationCodeRepository;


    //회원가입
    public boolean Signup(UserDTO.UserSignupRequest request){
        try {
            if (!emailVerficationService.isVerified(request.getEmail())) {
                return false;
            } else {
                request.setPassword(passwordEncoder.encode(request.getPassword()));
                User user = new User(request);
                userRepository.save(user);
                int deletecount = verifiacationCodeRepository.deleteAllByEmail(request.getEmail());
                System.out.println("Deleted " + deletecount + " verification codes for email: " + request.getEmail());
                return true;
            }
        }
          catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //로그인
    public UserDTO.UserLoginResponse login(UserDTO.UserLoginRequest request) throws IllegalAccessException {
        User user = userRepository.findByEmail(request.getEmail());
        if(user == null ){
            throw new IllegalAccessException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        String perfix = "Bearer ";
        String accesstoken = perfix + jwtUtil.createToken(user.getUsername(), user.getRole());
        UserDTO.UserSignupRequest loginResponseDTO = new UserDTO.UserSignupRequest(user.getUsername(),user.getEmail(), user.getPassword(), user.getRole());

        return new UserDTO.UserLoginResponse(user.getId(),loginResponseDTO.getEmail(),passwordEncoder.encode(loginResponseDTO.getPassword()), accesstoken,user.getRole());
    }

    //sns 로그인, 회원가입
    public UserDTO.UserLoginResponse SNSLogin(User snsuser, String provider){
        User user = userRepository.findByEmail(snsuser.getEmail());
        User newUser = User.builder()
                .role(UserRole.USER)
                .email(snsuser.getEmail())
                .provider(provider)
                .username(snsuser.getUsername())
                .password("SNS_LOGIN")
                .build();
        String token = jwtUtil.createToken(snsuser.getUsername(), UserRole.USER);
        if(user == null){
           userRepository.save(newUser);
            return new UserDTO.UserLoginResponse(newUser.getId(), newUser.getEmail(), newUser.getPassword(), token,newUser.getRole());
        }
        else{
            return new UserDTO.UserLoginResponse(user.getId(), user.getEmail(), user.getPassword(), token,newUser.getRole());
        }

    }
}
