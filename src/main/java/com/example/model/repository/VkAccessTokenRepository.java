package com.example.model.repository;

import com.example.model.entity.VkAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Description:
 * Creation date: 11.07.2016 9:34
 *
 * @author sks
 */
@Repository
public interface VkAccessTokenRepository extends JpaRepository<VkAccessToken, String>{

    VkAccessToken findOneBySessionId(String sessionId);
}
