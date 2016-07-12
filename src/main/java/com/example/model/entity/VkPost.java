package com.example.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

/**
 * Description:
 * Creation date: 12.07.2016 10:58
 *
 * @author sks
 */
@Entity
@Table(name = "vk_post")
public class VkPost {

    private Integer groupId;
    private Integer postId;

    @Id
    @Column(name = "post_id", nullable = false, unique = true)
    public Integer getPostId() {
        return postId;
    }

    @JsonProperty("id")
    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    @Basic
    @Column(name = "group_id", nullable = false, unique = false)
    public Integer getGroupId() {
        return groupId;
    }

    @JsonProperty("owner_id")
    public void setGroupId(Integer groupId) {
        if (groupId < 0)
            groupId *= -1;
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return String.format("VkPost(postId=%d groupId=%d)", postId, groupId);
    }
}
