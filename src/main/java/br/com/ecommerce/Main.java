package br.com.ecommerce;
import br.com.ecommerce.view.TelaCatalogo;
import br.com.ecommerce.view.TelaLogin;
import br.com.ecommerce.view.TelaProdutosRecentes;
import javax.swing.*;
import java.awt.*;
import br.com.ecommerce.view.TelaLoginCliente;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame tela = new JFrame("E-commerce");
            tela.setSize(500, 400);
            tela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            tela.setLocationRelativeTo(null);
            tela.setLayout(new BorderLayout(15, 15));
            JLabel titulo = new JLabel(
                    "BEM-VINDO AO E-COMMERCE",
                    SwingConstants.CENTER
            );

            titulo.setFont(
                    new Font("Arial", Font.BOLD, 23)
            );

            JLabel subtitulo = new JLabel(
                    "Escolha uma opção para continuar",
                    SwingConstants.CENTER
            );

            JButton btnCatalogo =
                    new JButton("Acessar Catálogo");
            JButton btnRecentes =
                    new JButton("Produtos Visualizados Recentemente");
            JButton btnAdmin =
                    new JButton("Área Administrativa");
            JButton btnLoginCliente = new JButton("Login do Cliente");
            btnLoginCliente.addActionListener(e -> {
                TelaLoginCliente loginCliente = new TelaLoginCliente(sessao -> {
                    JOptionPane.showMessageDialog(
                            null,
                            "Login realizado com sucesso!\n"
                            + "Bem-vindo: " + sessao.getEmail()
                    );
                });
                loginCliente.setVisible(true);
            });
            
            btnCatalogo.setFont(
                    new Font("Arial", Font.PLAIN, 16)
            );

            btnRecentes.setFont(
                    new Font("Arial", Font.PLAIN, 16)
            );

            btnAdmin.setFont(
                    new Font("Arial", Font.PLAIN, 16)
            );

            btnCatalogo.addActionListener(e -> {
                TelaCatalogo catalogo = new TelaCatalogo();
                catalogo.setVisible(true);
            });

            btnRecentes.addActionListener(e -> {
                TelaProdutosRecentes recentes =
                        new TelaProdutosRecentes();
                recentes.setVisible(true);
            });

            btnAdmin.addActionListener(e -> {
                TelaLogin login = new TelaLogin();
                login.setVisible(true);
            });

            JPanel painelCentral = new JPanel(
                    new GridLayout(4, 1, 10, 10)
            );

            painelCentral.setBorder(
                    BorderFactory.createEmptyBorder(
                            20, 45, 20, 45
                    )
            );
            
            painelCentral.add(btnLoginCliente);
            painelCentral.add(btnCatalogo);
            painelCentral.add(btnRecentes);
            painelCentral.add(btnAdmin);
            JPanel painelTitulo = new JPanel(
                    new GridLayout(2, 1, 5, 5)
            );

            painelTitulo.add(titulo);
            painelTitulo.add(subtitulo);
            tela.add(painelTitulo, BorderLayout.NORTH);
            tela.add(painelCentral, BorderLayout.CENTER);
            tela.setVisible(true);
        });
    }
}
