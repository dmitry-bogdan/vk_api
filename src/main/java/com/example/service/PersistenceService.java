package com.example.service;

import com.example.model.entity.VkAccessToken;
import com.example.model.entity.VkGroup;
import com.example.model.entity.VkPost;
import com.example.model.repository.VkAccessTokenRepository;
import com.example.model.repository.VkGroupRepository;
import com.example.model.repository.VkPostRepository;
import com.example.service.exception.VkAccessException;
import com.example.service.exception.VkDataException;
import com.example.service.exception.VkHttpResponseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * Creation date: 12.07.2016 8:35
 *
 * @author sks
 */
@Service
public class PersistenceService {

    private VkApiMethodInvoker apiMethodInvoker;
    private VkAccessTokenRepository accessTokenRepository;
    private VkGroupRepository groupRepository;
    private VkPostRepository postRepository;

    public void addAccessToken(String userCode, String userSessionId)
            throws VkHttpResponseException, IOException, VkDataException {

        VkAccessToken token = apiMethodInvoker.getAccessToken(userCode);
        if (token != null) {
            token.setSessionId(userSessionId);
            accessTokenRepository.save(token);
        } else
            throw new VkDataException("Error. Empty token was received");
    }

    public void addVkGroup(VkGroup group) throws VkHttpResponseException, IOException, VkDataException {
        String groupName = getGroupScreenName(group.getGroupURI());
        if (!StringUtils.isBlank(groupName)) {
            VkGroup newGroup = apiMethodInvoker.getVkGroup(groupName);
            if (newGroup != null) {
                newGroup.setGroupName(group.getGroupName());
                newGroup.setGroupURI(group.getGroupURI());
                if(newGroup.getGroupClosed() == 0){
                    List<VkPost> postList = apiMethodInvoker.getVkPost(newGroup.getGroupId(), 5, 0);
                    if (postList != null && !postList.isEmpty()) {
                        newGroup.setLastPostId(postList.get(0).getPostId());
                        addVkPost(postList.get(0));
                    }
                }
                System.out.println(groupRepository.save(newGroup));

            } else
                throw new VkDataException(String.format("Error. No matching group was found. URI=%s",
                        group.getGroupURI()));
        } else
            throw new VkDataException(String.format("Error. No matching group was found. URI=%s",
                    group.getGroupURI()));
    }

    public void addVkPost(VkPost post) {
        postRepository.save(post);
    }

    public void addNewPosts(Integer groupId) throws IOException, VkHttpResponseException, VkDataException {
        VkGroup group = groupRepository.findOne(groupId);
        if (group.getGroupClosed() > 0) return;
        group.setLastPostId(postRepository.findTopByGroupIdOrderByPostIdDesc(groupId).getPostId());
        group = groupRepository.save(group);

        List<VkPost> postList = new ArrayList<VkPost>();
        int offset = 0;
        boolean searching = true;
        try {
            while (searching) {
                for (VkPost post : apiMethodInvoker.getVkPost(groupId, 5, offset++ * 5)) {
                    if (post.getPostId() > group.getLastPostId())
                        postList.add(post);
                    else {
                        searching = false;
                        break;
                    }
                }
            }
        } catch (NullPointerException exception) {
            throw new VkDataException("Error. Empty data.");
        }
        if (!postList.isEmpty())
            postRepository.save(postList);
        System.out.println(String.format("%d new posts was added to groupId=%d", postList.size(), groupId));
    }

    public Map<String, Object> getGroupList(String userSessionId) throws VkAccessException, IOException, VkHttpResponseException {
        Map<String, Object> model = new HashMap<String, Object>();
        VkAccessToken accessToken = accessTokenRepository.findOneBySessionId(userSessionId);
        if (accessToken == null)
            throw new VkAccessException("Authentication error. Access denied.");
        if (!accessToken.getExpireDate().isAfter(LocalDateTime.now())) {
            accessTokenRepository.delete(accessToken);
            throw new VkAccessException("Authentication error. Session expired.");
        }

        List<VkGroup> groupList = groupRepository.findAll();
        Map<Integer, Boolean> signedMap = new HashMap<Integer, Boolean>();
        Map<Integer, List<VkPost>> newPostsList = new HashMap<Integer, List<VkPost>>();
        for (VkGroup group : groupList) {
            signedMap.put(group.getGroupId(),
                    apiMethodInvoker.getGroupIsMember(group.getGroupId(), accessToken.getAccessToken()));
        }
        for (VkGroup group : groupList){
            if (group.getGroupClosed() == 0){
                List<VkPost> list = postRepository.findByGroupIdAndPostIdGreaterThan(group.getGroupId(), group.getLastPostId());
                newPostsList.put(group.getGroupId(), list);
            }
        }
        model.put("groupList", groupList);
        model.put("signedMap", signedMap);
        model.put("newPostsList", newPostsList);
        return model;
    }

    private String getGroupScreenName(String uri) {
        String uriParts[] = uri.split("/");
        if (uriParts.length > 1) {
            return uriParts[uriParts.length - 1];
        }
        return null;
    }

    @Autowired
    public void setVkApiMethodInvoker(VkApiMethodInvoker apiMethodInvoker) {
        this.apiMethodInvoker = apiMethodInvoker;
    }

    @Autowired
    public void setAccessTokenRepository(VkAccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Autowired
    public void setGroupRepository(VkGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Autowired
    public void setVkPostRepository(VkPostRepository postRepository) {
        this.postRepository = postRepository;
    }
}
