package com.example.service;

import com.example.model.entity.VkAccessToken;
import com.example.model.entity.VkGroup;
import com.example.model.repository.VkAccessTokenRepository;
import com.example.model.repository.VkGroupRepository;
import com.example.service.exception.VkDataException;
import com.example.service.exception.VkHttpResponseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Description:
 * Creation date: 11.07.2016 9:37
 *
 * @author sks
 */
@Service
public class VkApiMethodInvoker {

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
    private VkAccessTokenRepository accessTokenRepository;
    private VkGroupRepository groupRepository;

    /*
    Proxy config
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

    public void getAccessToken(String sessionId, String code) throws VkHttpResponseException, IOException {

        HttpGet httpGet = new HttpGet(uriBuilder.getVkAccessTokenPage(code));
        httpGet.setConfig(requestConfig);

        HttpResponse httpResponse = null;
        VkAccessToken token = null;
        httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            token = mapper.readValue(httpResponse.getEntity().getContent(), VkAccessToken.class);
            token.setSessionId(sessionId);
            accessTokenRepository.save(token);
        } else {
            System.err.println(httpResponse.getStatusLine().toString());
            throw new VkHttpResponseException(httpResponse.getStatusLine().toString());
        }
    }

    public void getVkGroup(VkGroup inputGroup) throws VkHttpResponseException, VkDataException, IOException {
        HttpGet httpGet = new HttpGet(uriBuilder.getVkGroup(getGroupScreenName(inputGroup.getGroupURI())));
        httpGet.setConfig(requestConfig);
        HttpResponse httpResponse = null;
        httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            VkResponse<VkGroup> response = mapper.readValue(httpResponse.getEntity().getContent(),
                    new TypeReference<VkResponse<VkGroup>>(){});
            if (response.getArray() != null && response.getArray().length > 0) {
                VkGroup group = response.getArray()[0];
                group.setGroupName(inputGroup.getGroupName());
                group.setGroupURI(inputGroup.getGroupURI());
                System.out.println(groupRepository.save(group));
            }
            else throw new VkDataException(String.format("Error. No matching group was found. URI=%s",
                        inputGroup.getGroupURI()));
        } else {
            System.err.println(httpResponse.getStatusLine().toString());
            throw new VkHttpResponseException(httpResponse.getStatusLine().toString());
        }
    }

    
    private String getGroupScreenName(String uri) {
        String uriParts[] = uri.split("/");
        if (uriParts.length > 1) {
            return uriParts[uriParts.length - 1];
        }
        return null;

    }

    @Autowired
    public void setURI_Builder(URI_Builder uriBuilder) {
        this.uriBuilder = uriBuilder;
    }

    @Autowired
    public void setAccessTokenRepository(VkAccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Autowired
    public void setGroupRepository(VkGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

}
