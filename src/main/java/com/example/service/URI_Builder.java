package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Description: Построитель URI для вызова API
 * Creation date: 11.07.2016 8:40
 *
 * @author sks
 */
@Component
public class URI_Builder {

    @Value("${vk.application.id}")
    private String vkAppId;

    @Value("${vk.application.permissions}")
    private String vkPremissions;

    @Value("${vk.application.redirect_uri}")
    private String vkRedirectURI;

    @Value("${vk.application.api_version}")
    private String vkApiVersion;

    @Value("${vk.application.secret_key}")
    private String vkSecretKey;

    public String getVkAuthenticationURI(){
        return new StringBuilder("https://oauth.vk.com/authorize?")
                .append("client_id=").append(vkAppId)
                .append("&scope=").append(vkPremissions)
                .append("&redirect_uri=").append(vkRedirectURI)
                .append("&v=").append(vkApiVersion)
                .append("&display=page&")
                .append("&response_type=code").toString();

    }

    public String getVkAccessTokenPage(String code){
        return new StringBuilder("https://oauth.vk.com/access_token?")
                .append("client_id=").append(vkAppId)
                .append("&client_secret=").append(vkSecretKey)
                .append("&redirect_uri=").append(vkRedirectURI)
                .append("&code=").append(code).toString();
    }

    public String getVkGroup(String shortName){
        return new StringBuilder("https://api.vk.com/method/groups.getById?")
                .append("group_id=").append(shortName)
                .append("&v=").append(vkApiVersion).toString();
    }

    public String getVkPost(String groupId, String count, String offset){
        return new StringBuilder("https://api.vk.com/method/wall.get?")
                .append("owner_id=-").append(groupId)
                .append("&count=").append(count)
                .append("&offset=").append(offset)
                .append("&v=").append(vkApiVersion).toString();
    }

    public String getIsMemberURI(String groupId, String accessToken){
        return new StringBuilder("https://api.vk.com/method/groups.isMember?")
                .append("group_id=").append(groupId)
                .append("&access_token=").append(accessToken).toString();
    }
}