package br.com.ecommerce.view;

import br.com.ecommerce.dao.ProdutoDAO;
import br.com.ecommerce.model.Produto;
import br.com.ecommerce.service.ProdutoCacheService;
import br.com.ecommerce.service.CarrinhoService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TelaCatalogo extends JFrame {
    private JTable tabela;
    private DefaultTableModel modelo;
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final ProdutoCacheService cacheService =
            new ProdutoCacheService();
    private final CarrinhoService carrinhoService;
    private List<Produto> produtosExibidos = new ArrayList<>();
    private final NumberFormat moeda =
            NumberFormat.getCurrencyInstance(
                    new Locale("pt", "BR")
            );
    
public TelaCatalogo() {
    this(new CarrinhoService());
}

public TelaCatalogo(CarrinhoService carrinhoService) {
    this.carrinhoService = carrinhoService;
    setTitle("E-commerce - Catálogo de Produtos");
    setSize(950, 550);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    criarInterface();
    carregarProdutos();
}

    private void criarInterface() {
        setLayout(new BorderLayout(10, 10));
        JLabel titulo = new JLabel(
                "CATÁLOGO DE PRODUTOS",
                SwingConstants.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        modelo = new DefaultTableModel(
                new Object[]{
                    "Nome", "Descrição", "Preço",
                    "Categoria", "Estoque"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(
                    int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);
        tabela.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JButton btnAtualizar =
                new JButton("Atualizar Catálogo");
        JButton btnDetalhes =
                new JButton("Ver Produto");
        JButton btnAdicionar =
                new JButton("Adicionar ao Carrinho");
        JButton btnCarrinho =
                new JButton("Visualizar Carrinho");
        btnAtualizar.addActionListener(
                e -> carregarProdutos()
        );

        btnDetalhes.addActionListener(
                e -> visualizarProduto()
        );

        btnAdicionar.addActionListener(
                e -> adicionarAoCarrinho()
        );

        btnCarrinho.addActionListener(
                e -> abrirCarrinho()
        );

        JPanel painelBotoes = new JPanel();
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnDetalhes);
        painelBotoes.add(btnAdicionar);
        painelBotoes.add(btnCarrinho);
        add(titulo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);
        tabela.addMouseListener(
                new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(
                    java.awt.event.MouseEvent e) {

                if (e.getClickCount() == 2) {
                    visualizarProduto();
                }
            }
        });
    }

    private void carregarProdutos() {
        new SwingWorker<List<Produto>, Void>() {
            @Override
            protected List<Produto> doInBackground() {
                return produtoDAO.listar();
            }

            @Override
            protected void done() {
                try {
                    List<Produto> produtos = get();
                    modelo.setRowCount(0);
                    produtosExibidos.clear();
                    for (Produto produto : produtos) {
                        if (produto.isAtivo()) {
                            produtosExibidos.add(produto);
                            modelo.addRow(new Object[]{
                                produto.getNome(),
                                produto.getDescricao(),
                                moeda.format(produto.getPreco()),
                                produto.getCategoria(),
                                produto.getEstoque()
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            TelaCatalogo.this,
                            "Erro ao carregar produtos: "
                            + e.getMessage()
                    );
                }
            }

        }.execute();
    }

    private Produto obterProdutoSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecione um produto."
            );

            return null;
        }

        int indice = tabela.convertRowIndexToModel(linha);
        return produtosExibidos.get(indice);
    }

    private void visualizarProduto() {
        Produto selecionado = obterProdutoSelecionado();
        if (selecionado == null) {
            return;
        }

        try {
            cacheService.registrarVisualizacao(
                    selecionado.getId().toHexString()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Produto: " + selecionado.getNome()
                    + "\nDescrição: " + selecionado.getDescricao()
                    + "\nPreço: " + moeda.format(
                            selecionado.getPreco()
                    )
                    + "\nCategoria: " + selecionado.getCategoria()
                    + "\nEstoque: " + selecionado.getEstoque()
                    + "\n\nVisualização registrada no Redis!"
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao registrar visualização no Redis: "
                    + e.getMessage()
            );
        }
    }

    private void adicionarAoCarrinho() {
        Produto produto = obterProdutoSelecionado();
        if (produto == null) {
            return;
        }

        if (produto.getEstoque() <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Produto sem estoque disponível."
            );

            return;
        }

        JSpinner quantidade = new JSpinner(
                new SpinnerNumberModel(
                        1, 1, produto.getEstoque(), 1
                )
        );

        JPanel painel = new JPanel(new GridLayout(0, 1));
        painel.add(new JLabel(
                "Produto: " + produto.getNome()
        ));

        painel.add(new JLabel(
                "Preço: " + moeda.format(produto.getPreco())
        ));

        painel.add(new JLabel(
                "Estoque disponível: " + produto.getEstoque()
        ));

        painel.add(new JLabel("Quantidade:"));
        painel.add(quantidade);
        int opcao = JOptionPane.showConfirmDialog(
                this,
                painel,
                "Adicionar ao Carrinho",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (opcao == JOptionPane.OK_OPTION) {
            try {
                carrinhoService.adicionarProduto(
                        produto,
                        (Integer) quantidade.getValue()
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Produto adicionado ao carrinho!"
                );

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Erro: " + e.getMessage()
                );
            }
        }
    }

    private void abrirCarrinho() {
        TelaCarrinho telaCarrinho =
                new TelaCarrinho(carrinhoService);
        telaCarrinho.setVisible(true);
    }
}
