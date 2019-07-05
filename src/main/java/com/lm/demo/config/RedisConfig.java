package com.lm.demo.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author lm
 * @since 2019-07-03 17:06
 **/
@Configuration
public class RedisConfig {

    /**
     *  通过源码{@link RedisAutoConfiguration}可以看出，SpringBoot自动帮我们在容器中生成了一个RedisTemplate和一个
     *  StringRedisTemplate。但是，这个RedisTemplate的泛型是<Object,Object>，写代码不方便，需要写好多类型转换的代码；
     *  我们需要一个泛型为<String,Object>形式的RedisTemplate。并且，这个RedisTemplate没有设置数据存在Redis时，key及
     *  value的序列化方式。
     *  看到这个@ConditionalOnMissingBean注解后，就知道如果Spring容器中有了RedisTemplate对象了，这个自动配置的
     *  RedisTemplate不会实例化。因此我们可以直接自己写个配置类，配置RedisTemplate。
     *
     *  {@link RedisAutoConfiguration#redisTemplate(RedisConnectionFactory)}用 {@link ConditionalOnMissingBean}修饰的作用：
     * （1）@ConditionalOnMissingBean注解具体作用：在当前Spring上下文中不存在某个对象时，会实例化一个Bean。
     * （2）当依赖中加入Redis的start后，SpringBoot默认会自动实例化一个redisRemplate。但是，它会自动实例化的条件是当前
     *      上下文中没有redisRemplate对象。所以当我们自己手动给实例化一个redisRemplate对象后，SpringBoot的就不会再实例化
     *      一个redisRemplate对象了。
     * （3）为何通常要手动实例化一个redisRemplate对象？因为通常需要做一些定制化的东西，比如：key的序列化方式，value的序列化方式等。
     */
    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String,Object> redisTemplate (RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL , JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //key采用String的序列化方式
        redisTemplate.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // 注意设置下key和value的序列化方式，不然存到Redis的中数据看起来像乱码一下
        // value序列化方式采用jackson
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
