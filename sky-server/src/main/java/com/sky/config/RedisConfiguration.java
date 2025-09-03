package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("redisTemplate初始化");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 1. 定制ObjectMapper，解决LocalDateTime序列化问题
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册JavaTimeModule，支持Java 8时间类型
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用默认的时间戳序列化（否则LocalDateTime会被序列化为long型时间戳）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2. 构建支持时间类型的JSON序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 3. 设置序列化器
        template.setKeySerializer(new StringRedisSerializer());       // key：String序列化
        template.setValueSerializer(jsonSerializer);                  // value：JSON序列化（支持LocalDateTime）
        template.setHashKeySerializer(new StringRedisSerializer());   // hash key：String序列化
        template.setHashValueSerializer(jsonSerializer);              // hash value：JSON序列化（支持LocalDateTime）

        template.afterPropertiesSet();
        log.info("redisTemplate初始化完成");
        return template;
    }
}