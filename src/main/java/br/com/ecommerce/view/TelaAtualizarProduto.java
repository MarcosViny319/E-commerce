package br.com.ecommerce.view;

import br.com.ecommerce.dao.ProdutoDAO;
import br.com.ecommerce.model.Produto;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaAtualizarProduto extends JFrame {

    private JTable tabela;
    private DefaultTableModel modelo;

    private JTextField campoNome;
    private JTextField campoDescricao;
    private JTextField campoPreco;
    private JTextField campoCategoria;
    private JTextField campoImagem;
    private JTextField campoEstoque;
    private JCheckBox campoAtivo;
    private JButton botaoSalvar;

    public TelaAtualizarProduto() {

        setTitle("Atualizar Produto");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] colunas = {
            "ID", "Nome", "Descrição", "Preço",
            "Categoria", "Imagem", "Estoque", "Ativo"
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

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()
                    && tabela.getSelectedRow() >= 0) {
                preencherFormulario();
            }
        });

        JPanel painelFormulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );

        painelFormulario.setBorder(
                BorderFactory.createTitledBorder(
                        "Dados do Produto"
                )
        );

        campoNome = new JTextField();
        campoDescricao = new JTextField();
        campoPreco = new JTextField();
        campoCategoria = new JTextField();
        campoImagem = new JTextField();
        campoEstoque = new JTextField();
        campoAtivo = new JCheckBox("Produto ativo");

        painelFormulario.add(new JLabel("Nome:"));
        painelFormulario.add(campoNome);

        painelFormulario.add(new JLabel("Descrição:"));
        painelFormulario.add(campoDescricao);

        painelFormulario.add(new JLabel("Preço:"));
        painelFormulario.add(campoPreco);

        painelFormulario.add(new JLabel("Categoria:"));
        painelFormulario.add(campoCategoria);

        painelFormulario.add(new JLabel("URL da imagem:"));
        painelFormulario.add(campoImagem);

        painelFormulario.add(new JLabel("Estoque:"));
        painelFormulario.add(campoEstoque);

        painelFormulario.add(campoAtivo);
        painelFormulario.add(new JLabel(""));

        botaoSalvar = new JButton("SALVAR ALTERAÇÕES");
        botaoSalvar.setEnabled(false);

        botaoSalvar.addActionListener(e -> atualizarProduto());

        JPanel painelInferior = new JPanel(new BorderLayout());
        painelInferior.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        painelInferior.add(painelFormulario, BorderLayout.CENTER);
        painelInferior.add(botaoSalvar, BorderLayout.SOUTH);

        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);

        carregarProdutos();
    }

    private void carregarProdutos() {

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
                            produto.getDescricao(),
                            produto.getPreco(),
                            produto.getCategoria(),
                            produto.getImagem(),
                            produto.getEstoque(),
                            produto.isAtivo()
                        });
                    }

                } catch (Exception ex) {

    ex.printStackTrace();

    Throwable causa = ex.getCause();

    String mensagem = causa != null
            ? causa.getMessage()
            : ex.getMessage();

    JOptionPane.showMessageDialog(
            TelaAtualizarProduto.this,
            "Erro ao carregar produtos:\n" + mensagem,
            "Erro",
            JOptionPane.ERROR_MESSAGE
    );
}
            }
        };

        worker.execute();
    }

    private void preencherFormulario() {

        int linha = tabela.getSelectedRow();

        campoNome.setText(modelo.getValueAt(linha, 1).toString());
        campoDescricao.setText(modelo.getValueAt(linha, 2).toString());
        campoPreco.setText(modelo.getValueAt(linha, 3).toString());
        campoCategoria.setText(modelo.getValueAt(linha, 4).toString());

        Object imagem = modelo.getValueAt(linha, 5);
        campoImagem.setText(imagem == null ? "" : imagem.toString());

        campoEstoque.setText(modelo.getValueAt(linha, 6).toString());

        campoAtivo.setSelected(
                Boolean.TRUE.equals(modelo.getValueAt(linha, 7))
        );

        botaoSalvar.setEnabled(true);
    }

    private void atualizarProduto() {

        int linha = tabela.getSelectedRow();

        if (linha < 0) {
            return;
        }

        String nome = campoNome.getText().trim();
        String descricao = campoDescricao.getText().trim();
        String categoria = campoCategoria.getText().trim();
        String imagem = campoImagem.getText().trim();

        if (nome.isEmpty() || descricao.isEmpty()
                || categoria.isEmpty()
                || campoPreco.getText().trim().isEmpty()
                || campoEstoque.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Preencha todos os campos obrigatórios."
            );
            return;
        }

        double preco;
        int estoque;

        try {
            preco = Double.parseDouble(
                    campoPreco.getText().trim().replace(",", ".")
            );

            estoque = Integer.parseInt(
                    campoEstoque.getText().trim()
            );

            if (!Double.isFinite(preco) || preco <= 0 || estoque < 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Preço ou estoque inválido."
            );
            return;
        }

        ObjectId id = new ObjectId(
                modelo.getValueAt(linha, 0).toString()
        );

        Produto produto = new Produto(
                nome,
                descricao,
                preco,
                categoria,
                imagem,
                estoque,
                campoAtivo.isSelected()
        );

        produto.setId(id);

        
botaoSalvar.setEnabled(false);
botaoSalvar.setText("SALVANDO...");

SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

    @Override
    protected Boolean doInBackground() {
        return new ProdutoDAO().atualizar(produto);
    }

    @Override
    protected void done() {

        try {
            if (get()) {

                JOptionPane.showMessageDialog(
                        TelaAtualizarProduto.this,
                        "Produto atualizado com sucesso!"
                );

                limparFormulario();
                carregarProdutos();

            } else {

                JOptionPane.showMessageDialog(
                        TelaAtualizarProduto.this,
                        "Produto não encontrado no MongoDB.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            Throwable causa = ex.getCause();

            String mensagem = causa != null
                    ? causa.getMessage()
                    : ex.getMessage();

            JOptionPane.showMessageDialog(
                    TelaAtualizarProduto.this,
                    "Erro ao atualizar produto:\n" + mensagem,
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );

        } finally {

            botaoSalvar.setText("SALVAR ALTERAÇÕES");
            botaoSalvar.setEnabled(tabela.getSelectedRow() >= 0);
        }
    }
};

        worker.execute();
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoDescricao.setText("");
        campoPreco.setText("");
        campoCategoria.setText("");
        campoImagem.setText("");
        campoEstoque.setText("");
        campoAtivo.setSelected(false);
        tabela.clearSelection();
    }
}
