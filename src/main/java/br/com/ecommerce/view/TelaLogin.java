package br.com.ecommerce.view;

import br.com.ecommerce.service.SupabaseAuthService;
import javax.swing.*;
import java.awt.*;

public class TelaLogin extends JFrame {
    private JTextField campoEmail;
    private JPasswordField campoSenha;
    private JButton botaoEntrar;
    public TelaLogin() {
        setTitle("E-commerce - Login Administrativo");
        setSize(420, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel titulo = new JLabel("E-COMMERCE", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 26));
        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(titulo, gbc);
        JLabel subtitulo = new JLabel(
                "Acesso administrativo", JLabel.CENTER);
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 1;
        painel.add(subtitulo, gbc);
        campoEmail = new JTextField(25);
        gbc.gridy = 2;
        painel.add(new JLabel("E-mail:"), gbc);
        gbc.gridy = 3;
        painel.add(campoEmail, gbc);
        campoSenha = new JPasswordField(25);
        gbc.gridy = 4;
        painel.add(new JLabel("Senha:"), gbc);
        gbc.gridy = 5;
        painel.add(campoSenha, gbc);
        botaoEntrar = new JButton("ENTRAR");
        botaoEntrar.setBackground(new Color(30, 70, 130));
        botaoEntrar.setForeground(Color.WHITE);
        botaoEntrar.setFocusPainted(false);
        gbc.gridy = 6;
        painel.add(botaoEntrar, gbc);
        botaoEntrar.addActionListener(e -> realizarLogin());
        add(painel);
    }

private void realizarLogin() {
    String email = campoEmail.getText().trim();
    char[] senhaCaracteres = campoSenha.getPassword();
    if (email.isEmpty() || senhaCaracteres.length == 0) {
        JOptionPane.showMessageDialog(
                this,
                "Preencha o e-mail e a senha."
        );
        return;
    }

    String senha = new String(senhaCaracteres);
    botaoEntrar.setEnabled(false);
    botaoEntrar.setText("AUTENTICANDO...");
    SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
        @Override
        protected Boolean doInBackground() throws Exception {
            SupabaseAuthService auth = new SupabaseAuthService();
            return auth.loginComoAdmin(email, senha);
        }

        @Override
        protected void done() {
            try {
                boolean administrador = get();
                if (administrador) {
                    JOptionPane.showMessageDialog(
                            TelaLogin.this,
                            "Acesso administrativo autorizado!"
                    );
                    campoSenha.setText("");
                    PainelAdmin painel = new PainelAdmin();
                    painel.setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(
                            TelaLogin.this,
                            "Usuário autenticado, mas sem permissão de administrador.",
                            "Acesso negado",
                            JOptionPane.WARNING_MESSAGE
                    );
                    campoSenha.setText("");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Throwable causa = ex.getCause();
                String mensagem = causa != null
                        ? causa.getMessage()
                        : ex.getMessage();
                JOptionPane.showMessageDialog(
                        TelaLogin.this,
                        "Erro: " + mensagem,
                        "Falha no login",
                        JOptionPane.ERROR_MESSAGE
                );
                campoSenha.setText("");
            } finally {
                botaoEntrar.setEnabled(true);
                botaoEntrar.setText("ENTRAR");
            }
        }
    };
    worker.execute();
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TelaLogin().setVisible(true);
        });
    }
}
