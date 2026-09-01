package org.example.smilegate.user.service.OauthService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.smilegate.config.global.Oauth.OAuthService;
import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.domain.UserRole;
import org.example.smilegate.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
@Slf4j
@Service
@Transactional
public class KakaoOauthService implements OAuthService {

    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.client.secret}")
    private String clientSecret;

    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    @Override
    public String getAccessToken(UserDTO.SNSloginRequest loginrequest) {
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&code=" + loginrequest.getCode();


        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    @Override
    public User getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();

        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        Long kakaoId = ((Number) body.get("id")).longValue();

        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        if (email == null) {
            // 이메일 동의 안 했거나 미제공 시 대체 이메일 생성
            email = "kakao_" + kakaoId + "@kakao.local";
        }

        return User.builder()
                .email(email)
                .role(UserRole.USER)
                .username((String) profile.get("nickname"))
                .build();
    }

    @Override
    public String getProviderName() {
        return "kakao";
    }
}

