package cc.cannot.dict.business.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Configuration
    @ConditionalOnClass({
            RedisConnectionFactory.class,
            RedisTemplate.class
    })
    public static class RedisTemplateConfig {
        @Bean(
                name = {"redisNumberTemplate"}
        )
        public RedisTemplate<String, Number> redisNumberTemplate(RedisConnectionFactory redisConnectionFactory) {
            RedisTemplate<String, Number> redisTemplate = new RedisTemplate<>();
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Number.class));
            redisTemplate.setExposeConnection(true);
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            redisTemplate.afterPropertiesSet();
            return redisTemplate;
        }

        @Bean(
                name = {"redisStringTemplate"}
        )
        @Qualifier("redisStringTemplate")
        public RedisTemplate<String, String> redisStringTemplate(RedisConnectionFactory redisConnectionFactory) {
            RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            redisTemplate.setHashKeySerializer(new StringRedisSerializer());
            redisTemplate.setHashValueSerializer(new StringRedisSerializer());
            redisTemplate.setExposeConnection(true);
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            redisTemplate.afterPropertiesSet();
            return redisTemplate;
        }
    }
}
