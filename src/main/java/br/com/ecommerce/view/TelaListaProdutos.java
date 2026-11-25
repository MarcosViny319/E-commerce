package br.com.ecommerce.view;

import br.com.ecommerce.dao.ProdutoDAO;
import br.com.ecommerce.dao.ProdutoCacheDAO;
import br.com.ecommerce.model.Produto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaListaProdutos extends JFrame {

    private JTable tabela;
    private DefaultTableModel modelo;
    private JButton botaoAtualizar;
    private JButton botaoDetalhes;

    public TelaListaProdutos() {

        setTitle("Produtos Cadastrados");
        setSize(950, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] colunas = {
            "ID", "Nome", "Preço (R$)",
            "Categoria", "Estoque", "Ativo"
        };

        modelo = new DefaultTableModel(colunas, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);
        tabela.setRowHeight(28);
        tabela.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        botaoAtualizar = new JButton("Atualizar Lista");
        botaoDetalhes = new JButton("Ver Detalhes");

        botaoAtualizar.addActionListener(
                e -> carregarProdutos()
        );

        botaoDetalhes.addActionListener(
                e -> mostrarDetalhes()
        );

        JPanel painelInferior = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 15, 10)
        );

        painelInferior.add(botaoAtualizar);
        painelInferior.add(botaoDetalhes);

        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);

        carregarProdutos();
    }

    private void carregarProdutos() {

        botaoAtualizar.setEnabled(false);
        botaoDetalhes.setEnabled(false);

        SwingWorker<List<Produto>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<Produto> doInBackground() {

                ProdutoDAO dao = new ProdutoDAO();

                return dao.listar();
            }

            @Override
            protected void done() {

                try {

                    List<Produto> produtos = get();

                    modelo.setRowCount(0);

                    for (Produto produto : produtos) {

                        modelo.addRow(new Object[]{
                            produto.getId(),
                            produto.getNome(),
                            String.format(
                                java.util.Locale.forLanguageTag("pt-BR"),
                                "%.2f",
                                produto.getPreco()
                            ),
                            produto.getCategoria(),
                            produto.getEstoque(),
                            produto.isAtivo() ? "Sim" : "Não"
                        });
                    }

                    botaoDetalhes.setEnabled(true);

                } catch (Exception ex) {

                    ex.printStackTrace();

                    Throwable causa = ex.getCause();

                    String mensagem = causa != null
                            ? causa.getMessage()
                            : ex.getMessage();

                    JOptionPane.showMessageDialog(
                            TelaListaProdutos.this,
                            "Erro ao carregar produtos:\n" + mensagem,
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

    private void mostrarDetalhes() {

        int linha = tabela.getSelectedRow();

        if (linha == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Selecione um produto na tabela."
            );

            return;
        }

        String id = modelo.getValueAt(linha, 0).toString();

        String nome = modelo.getValueAt(linha, 1).toString();

        String preco = modelo.getValueAt(linha, 2).toString();

        String categoria = modelo.getValueAt(linha, 3).toString();

        String estoque = modelo.getValueAt(linha, 4).toString();

        String ativo = modelo.getValueAt(linha, 5).toString();

        // Registrar visualização no Redis
        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() {

                ProdutoCacheDAO cache = new ProdutoCacheDAO();

                cache.registrarVisualizacao(id);

                return null;
            }

            @Override
            protected void done() {

                try {

                    get();

                    System.out.println(
                            "Produto registrado no Redis: " + id
                    );

                } catch (Exception ex) {

                    ex.printStackTrace();

                    JOptionPane.showMessageDialog(
                            TelaListaProdutos.this,
                            "Não foi possível registrar a visualização "
                            + "no Redis.\n"
                            + "Detalhes: " + ex.getMessage(),
                            "Aviso de cache",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        };

        worker.execute();

        // Exibir detalhes do produto
        JOptionPane.showMessageDialog(
                this,
                "ID: " + id
                + "\nNome: " + nome
                + "\nPreço: R$ " + preco
                + "\nCategoria: " + categoria
                + "\nEstoque: " + estoque
                + "\nAtivo: " + ativo,
                "Detalhes do Produto",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
