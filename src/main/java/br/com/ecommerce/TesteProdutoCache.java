package br.com.ecommerce;

import br.com.ecommerce.dao.ProdutoCacheDAO;
import br.com.ecommerce.conexao.RedisConnection;

public class TesteProdutoCache {

    public static void main(String[] args) {

    ProdutoCacheDAO cache = new ProdutoCacheDAO();

    cache.limparRecentes();

    System.out.println("Histórico do Redis limpo!");
}
}