package ai.example.langchain4j.config;


import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "langchain4j.store")
public class StoreProperties {

    private RedisProperties redis;

    public RedisProperties getRedis() {
        return redis;
    }

    public void setRedis(RedisProperties redis) {
         this.redis = redis;
    }


    public static class RedisProperties {
        private String host;

        private int port;

        private String password;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

