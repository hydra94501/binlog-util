package org.example.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.host}")
    private String host;
    @Value("${elasticsearch.username}")
    private String username;
    @Value("${elasticsearch.password}")
    private String password;




    @Bean
    public RestClient restClient() {
        // 创建凭证提供器
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        // 创建 RestClient，连接到 Elasticsearch 服务并添加认证
        return RestClient.builder(
                        new HttpHost(host, 9200, "http")
                )
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                                .setMaxConnTotal(100)  // 最大连接数
                                .setMaxConnPerRoute(10)  // 每个路由的最大连接数
                )
                .setRequestConfigCallback(requestConfigBuilder ->
                        requestConfigBuilder.setConnectTimeout(5000)
                                .setSocketTimeout(60000)
                )
                .build();
    }




    @Bean
    public RestClientTransport transport(RestClient restClient) {
        // 使用 RestClient 创建 RestClientTransport
        // JacksonJsonpMapper 用于 JSON 数据与 Java 对象的转换
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置 Jackson 的命名策略为 SNAKE_CASE（下划线转驼峰）
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        // JacksonJsonpMapper 用于 JSON 数据与 Java 对象的转换
        JsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);
        return new RestClientTransport(restClient, jsonpMapper);
    }


    @Bean
    public ElasticsearchClient elasticsearchClient(RestClientTransport transport) {
        // 创建 ElasticsearchClient 用于执行 Elasticsearch 操作
        return new ElasticsearchClient(transport);
    }
}