package org.example.smilegate.config.global.Oauth;

import org.example.smilegate.user.domain.User;
import org.example.smilegate.user.dto.UserDTO;

public interface OAuthService {
    String getAccessToken(UserDTO.SNSloginRequest request);

    User getUserInfo(String accessToken);
    String getProviderName();

}