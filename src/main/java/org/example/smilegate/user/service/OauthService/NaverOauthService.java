package org.example.smilegate.user.service.OauthService;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
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

@NoArgsConstructor
@Service
@Transactional
public class NaverOauthService implements OAuthService {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;


    @Override
    public String getAccessToken(UserDTO.SNSloginRequest loginRequest) {
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + loginRequest.getCode();


        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    @Override
    public User getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        Map<String, Object> userdata = (Map<String, Object>) body.get("response");
        return User.builder()
                .email((String) userdata.get("email"))
                .role(UserRole.USER)
                .username((String) userdata.get("name"))
                .build();
    }
    public String getProviderName(){
        return "naver";
    };



}




