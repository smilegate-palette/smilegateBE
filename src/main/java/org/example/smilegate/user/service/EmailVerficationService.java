package org.example.smilegate.user.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.domain.VerificationCode;
import org.example.smilegate.user.repository.UserRepository;
import org.example.smilegate.user.repository.VerifiacationCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.InvalidObjectException;
import java.security.SecureRandom;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerficationService {
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final VerifiacationCodeRepository verifiacationCodeRepository;

    @Value("${spring.mail.username}")
    String username;

    //인증 번호 이메일 전송
    public void SendVerificationCode(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if(user != null){
            throw new DuplicateEmailException();
        }
        else{
            String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setCode(code);
            verificationCode.setEmail(email);
            verificationCode.setValidatetime(LocalDateTime.now().plusMinutes(5));
            verifiacationCodeRepository.save(verificationCode);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(username);
            message.setTo(email);
            message.setSubject("[Smilegate] 이메일 인증 코드");
            message.setText("인증코드: " + code + "\n5분 안에 입력해주세요.");
            javaMailSender.send(message);

        }

    }

    public void verify(String email, String code) throws Exception {
        VerificationCode saved = verifiacationCodeRepository.findTopByEmailOrderByIdDesc(email);
        if (saved == null) {
            throw new Exception("인증 번호 전송을 해주세요.");
        } else if (saved.isExpired()) throw new Exception("인증 번호가 만료되었습니다. 다시 인증 번호를 받아주세요.");
        else if (!saved.getCode().equals(code)) throw new Exception("인증 번호가 맞지 않습니다.");
        saved.setVerified(true);

    }

    public boolean isVerified(String email) {
        VerificationCode saved = verifiacationCodeRepository.findTopByEmailOrderByIdDesc(email);
        return saved != null && saved.isVerified();
    }
}
