package sample;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@SpringBootTest
class SampleMvcImperativeApplicationTests {

    @MockBean
    @SuppressWarnings("unused")
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void contextLoads() {
    }

}
