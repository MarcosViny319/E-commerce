package br.com.ecommerce.view;

import br.com.ecommerce.model.ItemCarrinho;
import br.com.ecommerce.model.SessaoUsuario;
import br.com.ecommerce.service.CarrinhoService;
import br.com.ecommerce.service.FinalizacaoCompraService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class TelaCarrinho extends JFrame {

    private final CarrinhoService carrinhoService;

    private final FinalizacaoCompraService finalizacaoService =
            new FinalizacaoCompraService();

    private JTable tabela;
    private DefaultTableModel modelo;
    private JLabel lblTotal;

    private JButton btnRemover;
    private JButton btnAtualizar;
    private JButton btnLimpar;
    private JButton btnFinalizar;

    private boolean compraEmAndamento = false;

    private final NumberFormat moeda =
            NumberFormat.getCurrencyInstance(
                    new Locale("pt", "BR")
            );

    public TelaCarrinho(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;

        setTitle("Meu Carrinho de Compras");
        setSize(850, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        criarInterface();
        atualizarTabela();
    }

    private void criarInterface() {

        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel(
                "MEU CARRINHO DE COMPRAS",
                SwingConstants.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 22));

        modelo = new DefaultTableModel(
                new Object[]{
                    "Produto",
                    "Quantidade",
                    "Preço Unitário",
                    "Subtotal"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);

        tabela.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        lblTotal = new JLabel("Total: R$ 0,00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel painelBotoes = new JPanel();

        btnRemover = new JButton("Remover Item");
        btnAtualizar = new JButton("Alterar Quantidade");
        btnLimpar = new JButton("Limpar Carrinho");
        btnFinalizar = new JButton("Finalizar Compra");

        btnRemover.addActionListener(e -> removerItem());
        btnAtualizar.addActionListener(e -> alterarQuantidade());
        btnLimpar.addActionListener(e -> limparCarrinho());
        btnFinalizar.addActionListener(e -> finalizarCompra());

        painelBotoes.add(btnRemover);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnLimpar);
        painelBotoes.add(btnFinalizar);

        JPanel painelInferior = new JPanel(new BorderLayout());

        painelInferior.add(lblTotal, BorderLayout.NORTH);
        painelInferior.add(painelBotoes, BorderLayout.CENTER);

        add(titulo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);
    }

    private void atualizarTabela() {

        modelo.setRowCount(0);

        List<ItemCarrinho> itens = carrinhoService.listarItens();

        for (ItemCarrinho item : itens) {

            modelo.addRow(new Object[]{
                item.getProduto().getNome(),
                item.getQuantidade(),
                moeda.format(item.getProduto().getPreco()),
                moeda.format(item.getSubtotal())
            });
        }

        lblTotal.setText(
                "Total: " + moeda.format(carrinhoService.calcularTotal())
        );
    }

    private ItemCarrinho obterItemSelecionado() {

        int linha = tabela.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecione um item do carrinho."
            );

            return null;
        }

        return carrinhoService.listarItens().get(
                tabela.convertRowIndexToModel(linha)
        );
    }

    private void removerItem() {

        ItemCarrinho item = obterItemSelecionado();

        if (item == null) {
            return;
        }

        String id = item.getProduto().getId().toHexString();

        carrinhoService.removerProduto(id);

        atualizarTabela();

        JOptionPane.showMessageDialog(
                this,
                "Produto removido do carrinho!"
        );
    }

    private void alterarQuantidade() {

        ItemCarrinho item = obterItemSelecionado();

        if (item == null) {
            return;
        }

        JSpinner quantidade = new JSpinner(
                new SpinnerNumberModel(
                        item.getQuantidade(),
                        1,
                        item.getProduto().getEstoque(),
                        1
                )
        );

        int opcao = JOptionPane.showConfirmDialog(
                this,
                quantidade,
                "Alterar Quantidade",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (opcao == JOptionPane.OK_OPTION) {

            try {

                carrinhoService.atualizarQuantidade(
                        item.getProduto().getId().toHexString(),
                        (Integer) quantidade.getValue()
                );

                atualizarTabela();

            } catch (Exception e) {

                JOptionPane.showMessageDialog(
                        this,
                        "Erro: " + e.getMessage()
                );
            }
        }
    }

    private void limparCarrinho() {

        int opcao = JOptionPane.showConfirmDialog(
                this,
                "Deseja remover todos os produtos?",
                "Limpar Carrinho",
                JOptionPane.YES_NO_OPTION
        );

        if (opcao == JOptionPane.YES_OPTION) {

            carrinhoService.limparCarrinho();

            atualizarTabela();
        }
    }

    private void finalizarCompra() {

        if (carrinhoService.listarItens().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Seu carrinho está vazio."
            );

            return;
        }

        int opcao = JOptionPane.showConfirmDialog(
                this,
                "Deseja continuar para a finalização da compra?",
                "Confirmar Compra",
                JOptionPane.YES_NO_OPTION
        );

        if (opcao != JOptionPane.YES_OPTION) {
            return;
        }

        TelaLoginCliente login = new TelaLoginCliente(
                this::processarCompra
        );

        login.setLocationRelativeTo(this);
        login.setVisible(true);
    }

    private void processarCompra(SessaoUsuario sessao) {

        if (compraEmAndamento) {
            return;
        }

        compraEmAndamento = true;

        final List<ItemCarrinho> itensCompra =
                carrinhoService.listarItens();

        final String totalCompra =
                moeda.format(carrinhoService.calcularTotal());

        alterarEstadoBotoes(false);

        setCursor(
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        );

        new SwingWorker<Long, Void>() {

            @Override
            protected Long doInBackground() throws Exception {

                return finalizacaoService.finalizarCompra(
                        sessao,
                        carrinhoService
                );
            }

            @Override
            protected void done() {

                setCursor(Cursor.getDefaultCursor());

                try {

                    long idPedido = get();

                    atualizarTabela();

                    JOptionPane.showMessageDialog(
                            TelaCarrinho.this,
                            "Compra realizada com sucesso!\n\n"
                            + "Número do pedido: " + idPedido
                            + "\nTotal: " + totalCompra,
                            "Compra Confirmada",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    int opcaoWhatsApp = JOptionPane.showConfirmDialog(
                            TelaCarrinho.this,
                            "Deseja abrir o WhatsApp da loja "
                            + "para enviar os detalhes do pedido?",
                            "Enviar pedido pelo WhatsApp",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (opcaoWhatsApp == JOptionPane.YES_OPTION) {

                        abrirWhatsApp(
                                idPedido,
                                itensCompra,
                                totalCompra
                        );
                    }

                    dispose();

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    compraEmAndamento = false;
                    alterarEstadoBotoes(true);

                    JOptionPane.showMessageDialog(
                            TelaCarrinho.this,
                            "Operação interrompida."
                    );

                } catch (ExecutionException e) {

                    compraEmAndamento = false;
                    alterarEstadoBotoes(true);

                    Throwable causa = e.getCause();

                    String mensagem = causa != null
                            && causa.getMessage() != null
                            ? causa.getMessage()
                            : "Ocorreu um erro inesperado.";

                    JOptionPane.showMessageDialog(
                            TelaCarrinho.this,
                            "Não foi possível finalizar a compra:\n"
                            + mensagem,
                            "Erro na Compra",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        }.execute();
    }

    private void abrirWhatsApp(
            long idPedido,
            List<ItemCarrinho> itens,
            String totalCompra) {

        try {

            String telefone = System.getenv("WHATSAPP_NUMERO_LOJA");

            if (telefone == null || telefone.trim().isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "O número do WhatsApp da loja ainda não foi configurado.",
                        "WhatsApp",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            telefone = telefone.replaceAll("\\D", "");

            if (!telefone.matches("\\d{12,13}")) {

                JOptionPane.showMessageDialog(
                        this,
                        "O número do WhatsApp da loja é inválido.\n"
                        + "Configure o número com DDI 55, DDD e telefone.",
                        "Número inválido",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            StringBuilder mensagem = new StringBuilder();

            mensagem.append("Olá! Gostaria de confirmar meu pedido.\n\n");
            mensagem.append("Pedido nº: ").append(idPedido).append("\n\n");
            mensagem.append("Produtos:\n");

            for (ItemCarrinho item : itens) {

                mensagem.append("- ")
                        .append(item.getProduto().getNome())
                        .append("\n");

                mensagem.append("  Quantidade: ")
                        .append(item.getQuantidade())
                        .append("\n");

                mensagem.append("  Preço unitário: ")
                        .append(moeda.format(
                                item.getProduto().getPreco()
                        ))
                        .append("\n");

                mensagem.append("  Subtotal: ")
                        .append(moeda.format(item.getSubtotal()))
                        .append("\n\n");
            }

            mensagem.append("Total da compra: ")
                    .append(totalCompra)
                    .append("\n\n");

            mensagem.append("Aguardo a confirmação. Obrigado!");

            String textoCodificado = URLEncoder.encode(
                    mensagem.toString(),
                    StandardCharsets.UTF_8
            );

            URI endereco = new URI(
                    "https://wa.me/"
                    + telefone
                    + "?text="
                    + textoCodificado
            );

            if (!Desktop.isDesktopSupported()
                    || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Não foi possível abrir o navegador neste computador.",
                        "WhatsApp",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            Desktop.getDesktop().browse(endereco);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "A compra foi registrada, mas não foi possível "
                    + "abrir o WhatsApp.\n\n"
                    + "Você pode acessar o WhatsApp manualmente.\n"
                    + "Detalhe: " + e.getMessage(),
                    "Erro ao abrir WhatsApp",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void alterarEstadoBotoes(boolean habilitado) {

        btnRemover.setEnabled(habilitado);
        btnAtualizar.setEnabled(habilitado);
        btnLimpar.setEnabled(habilitado);
        btnFinalizar.setEnabled(habilitado);
    }
}
