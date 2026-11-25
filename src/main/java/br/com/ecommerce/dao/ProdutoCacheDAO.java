package br.com.ecommerce.dao;

import br.com.ecommerce.conexao.RedisConnection;
import redis.clients.jedis.JedisPooled;

import java.util.List;

public class ProdutoCacheDAO {

    private static final String CHAVE =
            "ecommerce:produtos:recentes";

    private static final int LIMITE = 10;

    private static final int EXPIRACAO = 86400;

    public void registrarVisualizacao(String produtoId) {

        if (produtoId == null || produtoId.isBlank()) {
            return;
        }

        JedisPooled redis = RedisConnection.getClient();

        redis.lrem(CHAVE, 0, produtoId);

        redis.lpush(CHAVE, produtoId);

        redis.ltrim(CHAVE, 0, LIMITE - 1);

        redis.expire(CHAVE, EXPIRACAO);
    }

    public List<String> listarRecentes() {

        JedisPooled redis = RedisConnection.getClient();

        return redis.lrange(CHAVE, 0, LIMITE - 1);
    }

    public void limparRecentes() {

        JedisPooled redis = RedisConnection.getClient();

        redis.del(CHAVE);
    }
}
