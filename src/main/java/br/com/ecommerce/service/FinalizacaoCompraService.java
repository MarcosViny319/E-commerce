package br.com.ecommerce.service;

import br.com.ecommerce.dao.PedidoDAO;
import br.com.ecommerce.dao.ProdutoDAO;
import br.com.ecommerce.model.ItemCarrinho;
import br.com.ecommerce.model.Produto;
import br.com.ecommerce.model.SessaoUsuario;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FinalizacaoCompraService {

    private final ProdutoDAO produtoDAO;
    private final PedidoDAO pedidoDAO;

    public FinalizacaoCompraService() {
        this.produtoDAO = new ProdutoDAO();
        this.pedidoDAO = new PedidoDAO();
    }

    public long finalizarCompra(
            SessaoUsuario sessao,
            CarrinhoService carrinhoService
    ) throws Exception {

        if (sessao == null || !sessao.isCliente()) {
            throw new IllegalArgumentException(
                    "Faça login como cliente para continuar."
            );
        }

        if (carrinhoService == null) {
            throw new IllegalArgumentException(
                    "Carrinho inválido."
            );
        }

        List<ItemCarrinho> itens =
                carrinhoService.listarItens();

        if (itens.isEmpty()) {
            throw new IllegalArgumentException(
                    "O carrinho está vazio."
            );
        }

        List<ItemCarrinho> estoqueReduzido =
                new ArrayList<>();

        boolean pedidoRegistrado = false;

        try {
            BigDecimal total = BigDecimal.ZERO;

            for (ItemCarrinho item : itens) {

                if (item == null
                        || item.getProduto() == null
                        || item.getProduto().getId() == null
                        || item.getQuantidade() <= 0) {

                    throw new IllegalStateException(
                            "Existe um item inválido no carrinho."
                    );
                }

                Produto produtoAtual = produtoDAO.buscarPorId(
                        item.getProduto().getId()
                );

                if (produtoAtual == null || !produtoAtual.isAtivo()) {
                    throw new IllegalStateException(
                            "Produto indisponível: "
                            + item.getProduto().getNome()
                    );
                }

                if (produtoAtual.getEstoque()
                        < item.getQuantidade()) {

                    throw new IllegalStateException(
                            "Estoque insuficiente para: "
                            + produtoAtual.getNome()
                    );
                }

                item.setProduto(produtoAtual);

                BigDecimal preco = BigDecimal.valueOf(
                        produtoAtual.getPreco()
                );

                BigDecimal subtotal = preco.multiply(
                        BigDecimal.valueOf(item.getQuantidade())
                );

                total = total.add(subtotal);
            }

            for (ItemCarrinho item : itens) {

                boolean reduziu = produtoDAO.reduzirEstoque(
                        item.getProduto().getId(),
                        item.getQuantidade()
                );

                if (!reduziu) {
                    throw new IllegalStateException(
                            "Não foi possível reservar o estoque de: "
                            + item.getProduto().getNome()
                    );
                }

                estoqueReduzido.add(item);
            }

            long idPedido = pedidoDAO.registrarPedido(
                    sessao,
                    itens,
                    total
            );
            
            pedidoRegistrado = true;

            carrinhoService.limparCarrinho();

            return idPedido;

        } catch (Exception erro) {

            if (!pedidoRegistrado) {

                for (ItemCarrinho item : estoqueReduzido) {

                    try {
                        boolean repôs = produtoDAO.reporEstoque(
                                item.getProduto().getId(),
                                item.getQuantidade()
                        );

                        if (!repôs) {
                            erro.addSuppressed(
                                    new IllegalStateException(
                                            "Não foi possível repor o estoque de: "
                                            + item.getProduto().getNome()
                                    )
                            );
                        }

                    } catch (Exception erroReposicao) {
                        erro.addSuppressed(erroReposicao);
                    }
                }
            }

            throw erro;
        }
    }
}
