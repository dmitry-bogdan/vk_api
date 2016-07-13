package com.example.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

/**
 * Description: Пост
 * Creation date: 12.07.2016 10:58
 *
 * @author sks
 */
@Entity
@Table(name = "vk_post")
public class VkPost {

    private Integer id;
    private Integer postId;
    private VkGroup group;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "post_id", nullable = false, unique = false)
    public Integer getPostId() {
        return postId;
    }

    @JsonProperty("id")
    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    @ManyToOne(targetEntity = VkGroup.class)
    @JoinColumn(name = "group_id", updatable = true, insertable = true, referencedColumnName = "group_id")
    public VkGroup getGroup() {
        return group;
    }

    public void setGroup(VkGroup group) {
        this.group = group;
    }

    @Transient
    public String getUri(){
        return String.format("https://vk.com/wall-%d_%d", group.getGroupId(), postId);
    }
    @Override
    public String toString() {
        return String.format("VkPost(postId=%d groupId=%d)", postId, group.getGroupId());
    }
}
