package br.com.ecommerce.dao;

import br.com.ecommerce.conexao.SupabaseConnection;
import br.com.ecommerce.model.ItemCarrinho;
import br.com.ecommerce.model.SessaoUsuario;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PedidoDAO {

    public long registrarPedido(
            SessaoUsuario sessao,
            List<ItemCarrinho> itens,
            BigDecimal valorTotal
    ) throws SQLException {

        if (sessao == null || !sessao.isCliente()) {
            throw new IllegalArgumentException(
                    "É necessário estar autenticado como cliente."
            );
        }

        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException(
                    "O carrinho está vazio."
            );
        }

        if (valorTotal == null || valorTotal.signum() < 0) {
            throw new IllegalArgumentException(
                    "Valor total inválido."
            );
        }

        UUID idUsuario;

        try {
            idUsuario = UUID.fromString(sessao.getIdUsuario());
        } catch (IllegalArgumentException erro) {
            throw new IllegalArgumentException(
                    "O identificador do usuário é inválido.", erro
            );
        }

        String sqlPedido =
                "INSERT INTO pedidos "
                + "(id_usuario, data_pedido, valor_total, status) "
                + "VALUES (?, CURRENT_TIMESTAMP, ?, ?) "
                + "RETURNING id_pedido";

        String sqlItem =
                "INSERT INTO itens_pedido "
                + "(id_pedido, id_produto, nome_produto, "
                + "quantidade, preco_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexao =
                SupabaseConnection.conectar()) {

            conexao.setAutoCommit(false);

            try {
                long idPedido;

                try (PreparedStatement ps =
                        conexao.prepareStatement(sqlPedido)) {

                    ps.setObject(1, idUsuario);
                    ps.setBigDecimal(2, valorTotal);
                    ps.setString(3, "CONFIRMADO");

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "Não foi possível gerar o pedido."
                            );
                        }

                        idPedido = rs.getLong("id_pedido");
                    }
                }

                BigDecimal totalCalculado = BigDecimal.ZERO;

                try (PreparedStatement ps =
                        conexao.prepareStatement(sqlItem)) {

                    for (ItemCarrinho item : itens) {

                        if (item == null
                                || item.getProduto() == null
                                || item.getProduto().getId() == null
                                || item.getQuantidade() <= 0) {

                            throw new SQLException(
                                    "Existe um item inválido no carrinho."
                            );
                        }

                        BigDecimal precoUnitario =
                                BigDecimal.valueOf(
                                        item.getProduto().getPreco()
                                );

                        if (precoUnitario.signum() < 0) {
                            throw new SQLException(
                                    "O preço do produto é inválido."
                            );
                        }

                        BigDecimal subtotal =
                                precoUnitario.multiply(
                                        BigDecimal.valueOf(
                                                item.getQuantidade()
                                        )
                                );

                        totalCalculado =
                                totalCalculado.add(subtotal);

                        ps.setLong(1, idPedido);

                        ps.setString(
                                2,
                                item.getProduto().getId().toHexString()
                        );

                        ps.setString(
                                3,
                                item.getProduto().getNome()
                        );

                        ps.setInt(4, item.getQuantidade());
                        ps.setBigDecimal(5, precoUnitario);
                        ps.setBigDecimal(6, subtotal);

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                if (totalCalculado.compareTo(valorTotal) != 0) {
                    throw new SQLException(
                            "O valor total do pedido não corresponde "
                            + "à soma dos itens."
                    );
                }

                conexao.commit();

                return idPedido;

            } catch (Exception erro) {

                try {
                    conexao.rollback();
                } catch (SQLException erroRollback) {
                    erro.addSuppressed(erroRollback);
                }

                if (erro instanceof SQLException) {
                    throw (SQLException) erro;
                }

                throw new SQLException(
                        "Erro ao registrar o pedido.", erro
                );
            }
        }
    }
}