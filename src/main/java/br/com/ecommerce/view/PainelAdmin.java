package br.com.ecommerce.view;

import javax.swing.*;
import java.awt.*;

public class PainelAdmin extends JFrame {

    public PainelAdmin() {

        setTitle("E-commerce - Painel Administrativo");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(Color.WHITE);

        // CABEÇALHO
        JPanel painelCabecalho = new JPanel(new GridLayout(2, 1));
        painelCabecalho.setBackground(Color.WHITE);
        painelCabecalho.setBorder(
                BorderFactory.createEmptyBorder(25, 10, 15, 10)
        );

        JLabel titulo = new JLabel(
                "PAINEL ADMINISTRATIVO",
                JLabel.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(new Color(30, 70, 130));

        JLabel subtitulo = new JLabel(
                "Gerenciamento de produtos",
                JLabel.CENTER
        );

        subtitulo.setFont(new Font("Arial", Font.PLAIN, 16));

        painelCabecalho.add(titulo);
        painelCabecalho.add(subtitulo);

        // BOTÕES
        JPanel painelBotoes = new JPanel(
                new GridLayout(2, 2, 20, 20)
        );

        painelBotoes.setBackground(Color.WHITE);
        painelBotoes.setBorder(
                BorderFactory.createEmptyBorder(30, 50, 30, 50)
        );

        JButton botaoCadastrar = new JButton("Cadastrar Produto");
        JButton botaoListar = new JButton("Listar Produtos");
        JButton botaoAtualizar = new JButton("Atualizar Produto");
        JButton botaoExcluir = new JButton("Excluir Produto");

        Font fonteBotoes = new Font("Arial", Font.BOLD, 17);

        JButton[] botoes = {
            botaoCadastrar,
            botaoListar,
            botaoAtualizar,
            botaoExcluir
        };

        for (JButton botao : botoes) {
            botao.setFont(fonteBotoes);
            botao.setBackground(new Color(30, 70, 130));
            botao.setForeground(Color.WHITE);
            botao.setFocusPainted(false);
        }

        botaoCadastrar.addActionListener(e ->
                new TelaCadastroProduto().setVisible(true)
        );

        botaoListar.addActionListener(e ->
                new TelaListaProdutos().setVisible(true)
        );

        botaoAtualizar.addActionListener(e ->
                new TelaAtualizarProduto().setVisible(true)
        );

        botaoExcluir.addActionListener(e ->
                new TelaExcluirProduto().setVisible(true)
        );

        painelBotoes.add(botaoCadastrar);
        painelBotoes.add(botaoListar);
        painelBotoes.add(botaoAtualizar);
        painelBotoes.add(botaoExcluir);

        // RODAPÉ
        JPanel painelRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelRodape.setBackground(Color.WHITE);
        painelRodape.setBorder(
                BorderFactory.createEmptyBorder(10, 20, 20, 30)
        );

        JButton botaoSair = new JButton("Sair / Voltar ao Login");

        botaoSair.addActionListener(e -> {
            int opcao = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja encerrar a sessão administrativa?",
                    "Confirmar saída",
                    JOptionPane.YES_NO_OPTION
            );

            if (opcao == JOptionPane.YES_OPTION) {
                dispose();
                new TelaLogin().setVisible(true);
            }
        });

        painelRodape.add(botaoSair);

        painelPrincipal.add(painelCabecalho, BorderLayout.NORTH);
        painelPrincipal.add(painelBotoes, BorderLayout.CENTER);
        painelPrincipal.add(painelRodape, BorderLayout.SOUTH);

        add(painelPrincipal);
    }
}
