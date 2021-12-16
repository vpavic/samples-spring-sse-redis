package sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@SpringBootApplication(proxyBeanMethods = false)
@RestController
@EnableScheduling
public class SampleMvcImperativeApplication {

    private static final String SAMPLE_CHANNEL_PREFIX = "sample:topics:";

    public static void main(String[] args) {
        SpringApplication.run(SampleMvcImperativeApplication.class, args);
    }

    private static final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Autowired
    private RedisOperations<String, Event> eventRedisOperations;

    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(path = "/topics/{name:[a-z]{2}}")
    void createEvent(@PathVariable String name) {
        // https://github.com/spring-projects/spring-data-redis/issues/2209
        this.eventRedisOperations.convertAndSend(getChannelName(name), Event.generate());
    }

    @GetMapping(path = "/topics/{name:[a-z]{2}}")
    SseEmitter getEvents(@PathVariable String name) throws IOException {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.send(SseEmitter.event().comment("subscribed"));
        emitters.add(emitter);
        MessageListener messageListener = (message, pattern) -> {
            Event event = serialize(message);
            try {
                emitter.send(SseEmitter.event().data(event).id(event.id.toString()).name(event.type));
            }
            catch (IOException ex) {
                emitters.remove(emitter);
            }
        };
        this.redisMessageListenerContainer.addMessageListener(messageListener, ChannelTopic.of(getChannelName(name)));
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            this.redisMessageListenerContainer.removeMessageListener(messageListener);
        });
        return emitter;
    }

    private static String getChannelName(String topic) {
        return SAMPLE_CHANNEL_PREFIX + topic;
    }

    @Scheduled(fixedRateString = "PT30S")
    void keepAlive() {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().comment("keepalive"));
            }
            catch (IOException ex) {
                deadEmitters.add(emitter);
            }
        });
        emitters.removeAll(deadEmitters);
    }

    private Event serialize(Message message) {
        try {
            return this.objectMapper.readValue(message.getBody(), Event.class);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static class Event {

        @JsonProperty
        private final UUID id;

        @JsonProperty
        private final String type;

        Event(UUID id, String type) {
            this.id = id;
            this.type = type;
        }

        static Event generate() {
            return new Event(UUID.randomUUID(), "sample");
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class RedisConfiguration {

        @Bean
        RedisOperations<String, Event> eventRedisOperations(RedisConnectionFactory redisConnectionFactory,
                ObjectMapper objectMapper) {
            Jackson2JsonRedisSerializer<Event> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Event.class);
            jsonRedisSerializer.setObjectMapper(objectMapper);
            RedisTemplate<String, Event> eventRedisTemplate = new RedisTemplate<>();
            eventRedisTemplate.setConnectionFactory(redisConnectionFactory);
            eventRedisTemplate.setKeySerializer(RedisSerializer.string());
            eventRedisTemplate.setValueSerializer(jsonRedisSerializer);
            eventRedisTemplate.setHashKeySerializer(RedisSerializer.string());
            eventRedisTemplate.setHashValueSerializer(jsonRedisSerializer);
            return eventRedisTemplate;
        }

        @Bean
        RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
            RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
            redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
            return redisMessageListenerContainer;
        }

    }

}
