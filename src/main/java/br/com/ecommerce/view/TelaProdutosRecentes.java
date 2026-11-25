package br.com.ecommerce.view;

import br.com.ecommerce.dao.ProdutoDAO;
import br.com.ecommerce.dao.ProdutoCacheDAO;
import br.com.ecommerce.model.Produto;
import br.com.ecommerce.conexao.RedisConnection;

import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaProdutosRecentes extends JFrame {

    private JTable tabela;
    private DefaultTableModel modelo;

    private JButton botaoAtualizar;
    private JButton botaoLimpar;

    public TelaProdutosRecentes() {

        setTitle("Produtos Visualizados Recentemente");
        setSize(950, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] colunas = {
            "ID", "Nome", "Preço",
            "Categoria", "Estoque"
        };

        modelo = new DefaultTableModel(colunas, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);
        tabela.setRowHeight(28);

        botaoAtualizar = new JButton("Atualizar Histórico");
        botaoLimpar = new JButton("Limpar Histórico");

        botaoAtualizar.addActionListener(
                e -> carregarProdutos()
        );

        botaoLimpar.addActionListener(
                e -> limparHistorico()
        );

        JPanel painel = new JPanel();

        painel.add(botaoAtualizar);
        painel.add(botaoLimpar);

        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(painel, BorderLayout.SOUTH);

        carregarProdutos();
    }

    private void carregarProdutos() {

        botaoAtualizar.setEnabled(false);

        SwingWorker<List<Produto>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<Produto> doInBackground() {

                ProdutoCacheDAO cache = new ProdutoCacheDAO();
                ProdutoDAO dao = new ProdutoDAO();

                List<String> ids = cache.listarRecentes();

                List<Produto> produtos =
                        new java.util.ArrayList<>();

                for (String id : ids) {

                    try {

                        Produto produto = dao.buscarPorId(
                                new ObjectId(id)
                        );

                        if (produto != null) {
                            produtos.add(produto);
                        }

                    } catch (IllegalArgumentException ex) {

                        System.err.println(
                                "ID inválido no Redis: " + id
                        );
                    }
                }

                return produtos;
            }

            @Override
            protected void done() {

                try {

                    modelo.setRowCount(0);

                    for (Produto produto : get()) {

                        modelo.addRow(new Object[]{
                            produto.getId(),
                            produto.getNome(),
                            String.format(
                                java.util.Locale.forLanguageTag("pt-BR"),
                                "R$ %.2f",
                                produto.getPreco()
                            ),
                            produto.getCategoria(),
                            produto.getEstoque()
                        });
                    }

                    if (modelo.getRowCount() == 0) {

                        JOptionPane.showMessageDialog(
                                TelaProdutosRecentes.this,
                                "Nenhum produto visualizado recentemente."
                        );
                    }

                } catch (Exception ex) {

                    ex.printStackTrace();

                    JOptionPane.showMessageDialog(
                            TelaProdutosRecentes.this,
                            "Erro ao carregar histórico: "
                            + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {

                    botaoAtualizar.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void limparHistorico() {

        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Deseja limpar o histórico de produtos visualizados?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        botaoLimpar.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() {

                new ProdutoCacheDAO().limparRecentes();

                return null;
            }

            @Override
            protected void done() {

                try {

                    get();

                    modelo.setRowCount(0);

                    JOptionPane.showMessageDialog(
                            TelaProdutosRecentes.this,
                            "Histórico limpo com sucesso!"
                    );

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            TelaProdutosRecentes.this,
                            "Erro ao limpar histórico: "
                            + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {

                    botaoLimpar.setEnabled(true);
                }
            }
        };

        worker.execute();
    }
}
