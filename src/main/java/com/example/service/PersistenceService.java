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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: Работа с базой
 * Creation date: 12.07.2016 8:35
 *
 * @author sks
 */
@Service
public class PersistenceService {

    Logger LOG = Logger.getLogger(this.getClass());

    private VkApiMethodInvoker apiMethodInvoker;
    private VkAccessTokenRepository accessTokenRepository;
    private VkGroupRepository groupRepository;
    private VkPostRepository postRepository;

    /*
     * Добавление AccessToken
     * @param userCode код аутентификации юзера
     * @param userSessionId ид сессии юзера
     */
    public void addAccessToken(String userCode, String userSessionId)
            throws VkHttpResponseException, IOException, VkDataException {
        VkAccessToken token = apiMethodInvoker.getAccessToken(userCode);
        if (token != null) {
            token.setSessionId(userSessionId);
            token = accessTokenRepository.save(token);
            LOG.info(String.format("Access token inserted %s", token));
        } else
            throw new VkDataException("Error. Empty token was received.");
    }

    /*
     * Добавление группы
     * @param group группа
     */
    public void addVkGroup(VkGroup group) throws VkHttpResponseException, IOException, VkDataException {
        String groupName = getGroupScreenName(group.getGroupURI());
        if (!StringUtils.isBlank(groupName)) {
            VkGroup newGroup = apiMethodInvoker.getVkGroup(groupName);
            if (newGroup != null) {
                newGroup.setGroupName(group.getGroupName());
                newGroup.setGroupURI(group.getGroupURI());
                if (newGroup.getGroupClosed() == 0) {
                    List<VkPost> postList = apiMethodInvoker.getVkPost(newGroup.getGroupId(), 5, 0);
                    if (postList != null && !postList.isEmpty()) {
                        newGroup.setLastPostId(postList.get(0).getPostId());
                        postList.get(0).setGroup(newGroup);
                        newGroup = groupRepository.save(newGroup);
                        addVkPost(postList.get(0));
                    }
                } else
                    newGroup = groupRepository.save(newGroup);
                LOG.info(String.format("Group inserted %s", newGroup));

            } else
                throw new VkDataException(String.format("Error. No matching group was found. URI=%s",
                        group.getGroupURI()));
        } else
            throw new VkDataException(String.format("Error. No matching group was found. URI=%s",
                    group.getGroupURI()));
    }

    /*
     * Добавление поста
     * @param post пост
     */
    public void addVkPost(VkPost post) {
        post = postRepository.save(post);
        LOG.info(String.format("Post inserted %s", post));
    }

    /*
     * Проверка наличия глвых постов в группе
     * @param groupId ид группы
     */
    public void addNewPosts(Integer groupId) throws IOException, VkHttpResponseException, VkDataException {
        VkGroup group = groupRepository.findOne(groupId);
        if (group.getGroupClosed() > 0)
            return;
        group.setLastPostId(postRepository.findTopByGroupOrderByPostIdDesc(group).getPostId());
        group = groupRepository.save(group);

        List<VkPost> postList = new ArrayList<VkPost>();
        int offset = 0;
        boolean searching = true;
        try {
            while (searching) {
                for (VkPost post : apiMethodInvoker.getVkPost(groupId, 5, offset++ * 5)) {
                    if (post.getPostId() > group.getLastPostId()) {
                        post.setGroup(group);
                        postList.add(post);
                    } else {
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
        LOG.info(String.format("%d new posts was added to groupId=%d", postList.size(), groupId));
    }

    /*
     * Получение модели для отображения развернутого списка групп
     * @param userSessionId ид сессии юзера
     */
    public Map<String, Object> getGroupList(String userSessionId)
            throws VkAccessException, IOException, VkHttpResponseException {
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
        for (VkGroup group : groupList) {
            if (group.getGroupClosed() == 0) {
                List<VkPost> list = postRepository
                        .findByGroupAndPostIdGreaterThan(group, group.getLastPostId());
                newPostsList.put(group.getGroupId(), list);
            }
        }
        model.put("groupList", groupList);
        model.put("signedMap", signedMap);
        model.put("newPostsList", newPostsList);
        return model;
    }

    /*
     * Получение сокращенного имени группы
     * @param uri
     */
    private String getGroupScreenName(String uri) {
        Pattern pattern = Pattern.compile("public([0-9]+$)|[a-zA-Z0-9_]+$");
        Matcher matcher = pattern.matcher(uri.toLowerCase());
        String name = null;
        if (matcher.find()) {
            name = matcher.group(0).startsWith("public") ? matcher.group(0).replace("public", "") : matcher.group(0);
            LOG.info(String.format("Pattern matched for name %s", name));
        }

        return name;
    }

    @Scheduled(fixedRate = 5*60*1000)
    public void checkNewPosts() {
        LOG.info("Scheduled checkNewPosts() method started.");
        List<VkGroup> groupList = groupRepository.findAll();
        for (VkGroup group : groupList) {
            if (group.getGroupClosed() == 0)
                try {
                    addNewPosts(group.getGroupId());
                } catch (Exception exception) {
                    LOG.error(String.format("Error wile getting new posts VkGroup=%s Message=%s", group,
                            exception.getMessage()));
                    exception.printStackTrace();

                }
        }
        LOG.info("Scheduled checkNewPosts() method completed.");
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
