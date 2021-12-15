package sample;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@SpringBootTest
class SampleMvcReactiveApplicationTests {

    @MockBean
    @SuppressWarnings("unused")
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    @SuppressWarnings("unused")
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @Test
    void contextLoads() {
    }

}
