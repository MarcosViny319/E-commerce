package br.com.ecommerce.view;

import br.com.ecommerce.dao.ProdutoDAO;
import br.com.ecommerce.model.Produto;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaExcluirProduto extends JFrame {

    private JTable tabela;
    private DefaultTableModel modelo;
    private JButton botaoExcluir;

    public TelaExcluirProduto() {

        setTitle("Excluir Produtos");
        setSize(850, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] colunas = {
            "ID", "Nome", "Preço", "Categoria", "Estoque"
        };

        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);
        tabela.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tabela.setRowHeight(25);

        botaoExcluir = new JButton("EXCLUIR PRODUTO");
        botaoExcluir.setEnabled(false);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                botaoExcluir.setEnabled(
                        tabela.getSelectedRow() >= 0
                );
            }
        });

        botaoExcluir.addActionListener(e -> excluirProduto());

        JPanel painelInferior = new JPanel();
        painelInferior.add(botaoExcluir);

        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);

        carregarProdutos();
    }

    private void carregarProdutos() {

        botaoExcluir.setEnabled(false);

        SwingWorker<List<Produto>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<Produto> doInBackground() {
                return new ProdutoDAO().listar();
            }

            @Override
            protected void done() {

                try {
                    modelo.setRowCount(0);

                    for (Produto produto : get()) {

                        modelo.addRow(new Object[]{
                            produto.getId(),
                            produto.getNome(),
                            String.format("%.2f", produto.getPreco()),
                            produto.getCategoria(),
                            produto.getEstoque()
                        });
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            TelaExcluirProduto.this,
                            "Erro ao carregar produtos.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void excluirProduto() {

        int linha = tabela.getSelectedRow();

        if (linha < 0) {
            return;
        }

        String nome = modelo.getValueAt(linha, 1).toString();

        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente excluir o produto: "
                + nome + "?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        ObjectId id = new ObjectId(
                modelo.getValueAt(linha, 0).toString()
        );

        botaoExcluir.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() {
                return new ProdutoDAO().excluir(id);
            }

            @Override
            protected void done() {

                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                TelaExcluirProduto.this,
                                "Produto excluído com sucesso!"
                        );

                        carregarProdutos();

                    } else {
                        JOptionPane.showMessageDialog(
                                TelaExcluirProduto.this,
                                "Produto não encontrado."
                        );
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            TelaExcluirProduto.this,
                            "Erro ao excluir produto.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {
                    botaoExcluir.setEnabled(false);
                }
            }
        };

        worker.execute();
    }
}
