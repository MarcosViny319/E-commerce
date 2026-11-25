package br.com.ecommerce;

import br.com.ecommerce.dao.ProdutoDAO;
import br.com.ecommerce.model.Produto;
import br.com.ecommerce.conexao.MongoConnection;

public class TestProdutoDAO {

    public static void main(String[] args) {

        ProdutoDAO dao = new ProdutoDAO();

        Produto produto = new Produto(
                "Mouse Gamer Teste",
                "Mouse gamer RGB para teste do CRUD",
                150.00,
                "Perifericos",
                "https://exemplo.com/mouse.jpg",
                20,
                true
        );

        try {

            System.out.println("\n=== CADASTRAR PRODUTO ===");

            dao.cadastrar(produto);

            System.out.println("Produto cadastrado!");
            System.out.println("ID: " + produto.getId());

            System.out.println("\n=== LISTAR PRODUTOS ===");

            for (Produto p : dao.listar()) {
                System.out.println(p);
            }

            System.out.println("\n=== BUSCAR PRODUTO ===");

            Produto encontrado = dao.buscarPorId(produto.getId());

            if (encontrado != null) {
                System.out.println("Produto encontrado: " + encontrado);
            }

            System.out.println("\n=== ATUALIZAR PRODUTO ===");

            produto.setPreco(180.00);
            produto.setEstoque(30);

            boolean atualizado = dao.atualizar(produto);

            System.out.println("Atualizado: " + atualizado);

            System.out.println("\n=== EXCLUIR PRODUTO ===");

            boolean excluido = dao.excluir(produto.getId());

            System.out.println("Excluido: " + excluido);

        } catch (Exception e) {

            System.out.println("Erro durante o teste:");
            e.printStackTrace();

        } finally {

            MongoConnection.closeConnection();
        }
    }
}
