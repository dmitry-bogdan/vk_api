package com.example.model.repository;

import com.example.model.entity.VkPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Description:
 * Creation date: 12.07.2016 11:46
 *
 * @author sks
 */
@Repository
public interface VkPostRepository extends JpaRepository<VkPost, Integer>{
    VkPost findTopByGroupIdOrderByPostIdDesc(Integer groupId);
    List<VkPost> findByGroupIdAndPostIdGreaterThan(Integer groupId, Integer postId);
}
