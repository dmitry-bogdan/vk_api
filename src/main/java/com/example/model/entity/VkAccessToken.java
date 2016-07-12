package com.example.model.entity;

import com.example.model.LocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Description: Токен доступа
 * Creation date: 11.07.2016 8:30
 *
 * @author sks
 */
@Entity
@Table(name = "vk_access_token")
public class VkAccessToken {

    private String sessionId;
    private String accessToken;
    private LocalDateTime expireDate;
    private Integer userId;

    @Id
    @Column(name = "session_id", nullable = false, unique = true)
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Basic
    @Column(name = "access_token", nullable = false, unique = false)
    public String getAccessToken() {
        return accessToken;
    }
    @JsonProperty("access_token")
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "expire_date", nullable = false, unique = false)
    public LocalDateTime getExpireDate() {
        return expireDate;
    }
    public void setExpireDate(LocalDateTime expireDate) {
        this.expireDate = expireDate;
    }

    @Transient
    @JsonProperty("expires_in")
    public void setExpireDate(int seconds){
        this.expireDate = LocalDateTime.now().plusSeconds(seconds);
    }
    @Basic
    @Column(name = "user_id", nullable = false, unique = false)
    public Integer getUserId() {
        return userId;
    }
    @JsonProperty("user_id")
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString(){
        return String.format("VkAccessToken(sessionId=%s accessToken=%s expireDate=%s userId=%d)",
                sessionId, accessToken, expireDate, userId);
    }
}
