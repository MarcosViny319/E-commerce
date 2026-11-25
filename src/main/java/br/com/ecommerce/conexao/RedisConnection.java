package br.com.ecommerce.conexao;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

public class RedisConnection {

    private static JedisPooled redisClient;

    public static synchronized JedisPooled getClient() {

        if (redisClient == null) {

            String host = System.getenv("REDIS_HOST");
            String portText = System.getenv("REDIS_PORT");
            String username = System.getenv("REDIS_USERNAME");
            String password = System.getenv("REDIS_PASSWORD");

            if (host == null || portText == null
                    || username == null || password == null) {
                throw new RuntimeException(
                    "Configure as variáveis de ambiente do Redis Cloud."
                );
            }

            int port = Integer.parseInt(portText);

            DefaultJedisClientConfig config =
                DefaultJedisClientConfig.builder()
                    .user(username)
                    .password(password)
                    .ssl(false)
                    .build();

            redisClient = new JedisPooled(
                new HostAndPort(host, port), config
            );
        }

        return redisClient;
    }

    public static void closeConnection() {
        if (redisClient != null) {
            redisClient.close();
            redisClient = null;
        }
    }
}
