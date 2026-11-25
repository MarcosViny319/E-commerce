package br.com.ecommerce;

import br.com.ecommerce.conexao.RedisConnection;
import br.com.ecommerce.service.ProdutoCacheService;
import java.util.List;

public class TestProdutoCache {

    public static void main(String[] args) {

        ProdutoCacheService cache = new ProdutoCacheService();

        try {

            System.out.println("=== TESTE DO CACHE REDIS ===");

            cache.registrarVisualizacao("PRODUTO-001");
            cache.registrarVisualizacao("PRODUTO-002");
            cache.registrarVisualizacao("PRODUTO-003");

            System.out.println("Produtos registrados!");

            List<String> produtos = cache.listarRecentes();

            System.out.println("\nProdutos visualizados recentemente:");

            for (String produto : produtos) {
                System.out.println(produto);
            }

        } catch (Exception e) {

            System.out.println("Erro no teste: " + e.getMessage());

        } finally {
            RedisConnection.closeConnection();
        }
    }
}
