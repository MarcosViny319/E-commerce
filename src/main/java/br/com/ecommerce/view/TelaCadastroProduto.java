package br.com.ecommerce.view;

import br.com.ecommerce.dao.ProdutoDAO;
import br.com.ecommerce.model.Produto;
import javax.swing.*;
import java.awt.*;

public class TelaCadastroProduto extends JFrame {

    private JTextField campoNome;
    private JTextArea campoDescricao;
    private JTextField campoPreco;
    private JTextField campoCategoria;
    private JTextField campoImagem;
    private JTextField campoEstoque;
    private JCheckBox campoAtivo;
    private JButton botaoSalvar;

    public TelaCadastroProduto() {

        setTitle("Cadastrar Produto");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(550, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel painel = new JPanel(new GridLayout(0, 2, 10, 12));
        painel.setBorder(
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        );
        painel.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("CADASTRO DE PRODUTO");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        campoNome = new JTextField();
        campoDescricao = new JTextArea(4, 20);
        campoDescricao.setLineWrap(true);
        campoDescricao.setWrapStyleWord(true);

        campoPreco = new JTextField();
        campoCategoria = new JTextField();
        campoImagem = new JTextField();
        campoEstoque = new JTextField();

        campoAtivo = new JCheckBox("Produto ativo", true);
        campoAtivo.setBackground(Color.WHITE);

        botaoSalvar = new JButton("SALVAR PRODUTO");

        painel.add(titulo);
        painel.add(new JLabel(""));

        painel.add(new JLabel("Nome:"));
        painel.add(campoNome);

        painel.add(new JLabel("Descrição:"));
        painel.add(new JScrollPane(campoDescricao));

        painel.add(new JLabel("Preço (R$):"));
        painel.add(campoPreco);

        painel.add(new JLabel("Categoria:"));
        painel.add(campoCategoria);

        painel.add(new JLabel("URL da imagem:"));
        painel.add(campoImagem);

        painel.add(new JLabel("Estoque:"));
        painel.add(campoEstoque);

        painel.add(campoAtivo);
        painel.add(new JLabel(""));

        painel.add(new JLabel(""));
        painel.add(botaoSalvar);

        botaoSalvar.addActionListener(e -> cadastrarProduto());

        add(painel);
    }

    private void cadastrarProduto() {

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
                    "Informe um preço válido e um estoque inteiro não negativo.",
                    "Dados inválidos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Produto produto = new Produto(
                nome,
                descricao,
                preco,
                categoria,
                imagem,
                estoque,
                campoAtivo.isSelected()
        );

        botaoSalvar.setEnabled(false);
        botaoSalvar.setText("SALVANDO...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() {

                ProdutoDAO dao = new ProdutoDAO();
                dao.cadastrar(produto);

                return null;
            }

            @Override
            protected void done() {

                try {
                    get();

                    JOptionPane.showMessageDialog(
                            TelaCadastroProduto.this,
                            "Produto cadastrado com sucesso!\nID: "
                            + produto.getId(),
                            "Cadastro concluído",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    dispose();

                } catch (Exception ex) {
                    
                    ex.printStackTrace();
                    
                    Throwable causa = ex.getCause();
                    
                    String mensagem = causa != null
                            ? causa.getMessage()
                            : ex.getMessage();
                    JOptionPane.showMessageDialog(
                            TelaCadastroProduto.this,
                            "Erro ao cadastrar produto:\n" + mensagem,
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                    
                } finally {
                    botaoSalvar.setEnabled(true);
                    botaoSalvar.setText("SALVAR PRODUTO");                    
                }
            }
        };

        worker.execute();
    }
}
