package com.example.service;

import com.example.model.entity.VkAccessToken;
import com.example.model.entity.VkGroup;
import com.example.model.entity.VkPost;
import com.example.service.exception.VkHttpResponseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: Вызовы Апи Vk
 * Creation date: 11.07.2016 9:37
 *
 * @author sks
 */
@Service
public class VkApiMethodInvoker {

    Logger LOG = Logger.getLogger(this.getClass());
    
    @Value("${app.proxy.isEnabled}")
    private Boolean isProxyEnabled;
    @Value("${app.proxy.host}")
    private String proxyHost;
    @Value("${app.proxy.port}")
    private int proxyPort;
    @Value("${app.proxy.username}")
    private String proxyUsername;
    @Value("${app.proxy.pass}")
    private String proxyPass;

    private HttpClient httpClient;
    private RequestConfig requestConfig;
    private ObjectMapper mapper = new ObjectMapper();

    private URI_Builder uriBuilder;

    /*
    Конфигурация прокси
     */
    @PostConstruct
    public void init() {
        if (isProxyEnabled) {
            CredentialsProvider credentials = new BasicCredentialsProvider();
            credentials.setCredentials(
                    new AuthScope(proxyHost, proxyPort),
                    new UsernamePasswordCredentials(proxyUsername, proxyPass));
            httpClient = HttpClientBuilder.create()
                    .setDefaultCredentialsProvider(credentials)
                    .build();
            HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
            requestConfig = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
        } else {
            httpClient = HttpClientBuilder.create()
                    .build();
            requestConfig = RequestConfig.custom()
                    .build();
        }
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /*
     * Получить токена доступа
     * @param code код аутентификации юзера
     */
    public VkAccessToken getAccessToken(String code) throws VkHttpResponseException, IOException {

        HttpGet httpGet = new HttpGet(uriBuilder.getVkAccessTokenPage(code));
        httpGet.setConfig(requestConfig);

        HttpResponse httpResponse = null;
        VkAccessToken token = null;
        httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            token = mapper.readValue(httpResponse.getEntity().getContent(), VkAccessToken.class);
        } else {
            throw new VkHttpResponseException(httpResponse.getStatusLine().toString());
        }
        return token;
    }

    /*
     * Получить группу
     * @param groupName короткое имя группы(из URI)
     */
    public VkGroup getVkGroup(String groupName) throws VkHttpResponseException, IOException {
        HttpGet httpGet = new HttpGet(uriBuilder.getVkGroup(groupName));
        httpGet.setConfig(requestConfig);
        HttpResponse httpResponse = null;
        httpResponse = httpClient.execute(httpGet);
        VkGroup group = null;
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            JsonNode root = mapper.readTree(httpResponse.getEntity().getContent());
            JsonNode response = root.path("response");
            if (response.isArray()) {
                group = mapper.treeToValue(response.get(0), VkGroup.class);
            }
        } else {
            throw new VkHttpResponseException(httpResponse.getStatusLine().toString());
        }
        return group;
    }

    /*
     * Получить пост
     * @param groupId ид группы
     * @param count количество постов
     * @param offset сдвиг
     */
    public List<VkPost> getVkPost(Integer groupId, Integer count, Integer offset)
            throws IOException, VkHttpResponseException {
        HttpGet httpGet = new HttpGet(uriBuilder.getVkPost(groupId.toString(), count.toString(), offset.toString()));
        httpGet.setConfig(requestConfig);
        HttpResponse httpResponse = null;
        httpResponse = httpClient.execute(httpGet);
        List<VkPost> postList = new ArrayList<VkPost>();
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            JsonNode root = mapper.readTree(httpResponse.getEntity().getContent());
            JsonNode response = root.path("response");
            JsonNode items = response.path("items");
            if (items.isArray()) {
                for (JsonNode node : items) {
                    // Игнор прикрепленных
                    if (node.path("is_pinned").asInt() != 1)
                        postList.add(mapper.treeToValue(node, VkPost.class));
                }
            }
        } else {
            throw new VkHttpResponseException(httpResponse.getStatusLine().toString());
        }
        return postList;
    }

    /*
     * Является ли юзер членом группы
     * @param groupId ид группы
     * @param accessToken токен доступа юзера
     */
    public Boolean getGroupIsMember(Integer groupId, String accessToken) throws IOException, VkHttpResponseException {
        HttpGet httpGet = new HttpGet(uriBuilder.getIsMemberURI(groupId.toString(), accessToken));
        httpGet.setConfig(requestConfig);
        HttpResponse httpResponse = null;
        httpResponse = httpClient.execute(httpGet);
        Boolean isMember = null;
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            JsonNode root = mapper.readTree(httpResponse.getEntity().getContent());
            JsonNode response = root.path("response");
            isMember =  response.asInt() == 1;
        } else {
            System.err.println(httpResponse.getStatusLine().toString());
            throw new VkHttpResponseException(httpResponse.getStatusLine().toString());
        }
        return isMember;
    }
    
    @Autowired
    public void setURI_Builder(URI_Builder uriBuilder) {
        this.uriBuilder = uriBuilder;
    }

}
