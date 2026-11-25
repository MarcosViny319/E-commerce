package br.com.ecommerce.service;

import br.com.ecommerce.model.ItemCarrinho;
import br.com.ecommerce.model.Produto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CarrinhoService {
    private final List<ItemCarrinho> itens = new ArrayList<>();
    public void adicionarProduto(Produto produto, int quantidade) {
        if (produto == null || quantidade <= 0) {
            throw new IllegalArgumentException(
                "Produto ou quantidade inválidos."
            );
        }

        ItemCarrinho existente = buscarItem(
            produto.getId().toHexString()
        );

        int novaQuantidade = quantidade;

        if (existente != null) {
            novaQuantidade += existente.getQuantidade();
        }

        if (novaQuantidade > produto.getEstoque()) {
            throw new IllegalArgumentException(
                "Quantidade superior ao estoque disponível."
            );
        }

        if (existente == null) {
            itens.add(new ItemCarrinho(produto, quantidade));
        } else {
            existente.setQuantidade(novaQuantidade);
        }
    }

    public boolean removerProduto(String produtoId) {
        return itens.removeIf(item ->
            item.getProduto().getId().toHexString()
                .equals(produtoId)
        );
    }

    public void atualizarQuantidade(
        String produtoId, int quantidade
    ) {
        ItemCarrinho item = buscarItem(produtoId);
        if (item == null) {
            throw new IllegalArgumentException(
                "Produto não encontrado no carrinho."
            );
        }

        if (quantidade <= 0 ||
            quantidade > item.getProduto().getEstoque()) {
            throw new IllegalArgumentException(
                "Quantidade inválida."
            );
        }

        item.setQuantidade(quantidade);
    }

    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrinho item : itens) {
            total = total.add(
                BigDecimal.valueOf(item.getSubtotal())
            );
        }
        return total;
    }

    public List<ItemCarrinho> listarItens() {
        return Collections.unmodifiableList(
            new ArrayList<>(itens)
        );
    }

    public void limparCarrinho() {
        itens.clear();
    }

    private ItemCarrinho buscarItem(String produtoId) {
        for (ItemCarrinho item : itens) {
            if (item.getProduto().getId()
                    .toHexString().equals(produtoId)) {
                return item;
            }
        }
        return null;
    }
}
