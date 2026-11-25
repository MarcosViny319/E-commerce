package br.com.ecommerce;

import br.com.ecommerce.conexao.RedisConnection;
import redis.clients.jedis.JedisPooled;

public class TestRedisConnection {

    public static void main(String[] args) {

        try {

            JedisPooled redis = RedisConnection.getClient();

            String resposta = redis.ping();

            System.out.println("=== TESTE REDIS CLOUD ===");
            System.out.println("Conexao realizada com sucesso!");
            System.out.println("Resposta do Redis: " + resposta);

        } catch (Exception e) {

            System.out.println("Erro ao conectar ao Redis:");
            e.printStackTrace();

        } finally {
            RedisConnection.closeConnection();
        }
    }
}
