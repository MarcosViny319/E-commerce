package br.com.ecommerce.service;
import br.com.ecommerce.conexao.RedisConnection;
import java.util.List;
import redis.clients.jedis.JedisPooled;
public class ProdutoCacheService {
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

    public void limparHistorico() {
        JedisPooled redis = RedisConnection.getClient();
        redis.del(CHAVE);
    }
}
