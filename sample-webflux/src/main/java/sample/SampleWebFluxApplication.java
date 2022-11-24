package sample;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication(proxyBeanMethods = false)
@RestController
public class SampleWebFluxApplication {

    private static final String SAMPLE_CHANNEL_PREFIX = "sample:topics:";

    public static void main(String[] args) {
        SpringApplication.run(SampleWebFluxApplication.class, args);
    }

    @Autowired
    private ReactiveRedisOperations<String, Event> eventRedisOperations;

    @PostMapping(path = "/topics/{name:[a-z]{2}}")
    Mono<Map<String, Long>> createEvent(@PathVariable String name) {
        return this.eventRedisOperations.convertAndSend(getChannelName(name), Event.generate())
                .map(count -> Map.of("subscriberCount", count));
    }

    @GetMapping(path = "/topics/{name:[a-z]{2}}")
    Flux<ServerSentEvent<Event>> getEvents(@PathVariable String name) {
        return Mono.just(ServerSentEvent.<Event>builder().comment("subscribed").build())
                .mergeWith(Flux.interval(Duration.ofSeconds(30L))
                        .map(i -> ServerSentEvent.<Event>builder().comment("keepalive").build()))
                .mergeWith(this.eventRedisOperations.listenToChannel(getChannelName(name))
                        .map(ReactiveSubscription.Message::getMessage)
                        .map(event -> ServerSentEvent.builder(event).id(event.id.toString()).event(event.type).build()));
    }

    private static String getChannelName(String topic) {
        return SAMPLE_CHANNEL_PREFIX + topic;
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
        ReactiveRedisOperations<String, Event> eventRedisOperations(
                ReactiveRedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
            Jackson2JsonRedisSerializer<Event> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper,
                    Event.class);
            return new ReactiveRedisTemplate<>(redisConnectionFactory,
                    RedisSerializationContext.<String, Event>newSerializationContext()
                            .key(RedisSerializer.string())
                            .value(jsonRedisSerializer)
                            .hashKey(RedisSerializer.string())
                            .hashValue(jsonRedisSerializer)
                            .build());
        }

    }

}
