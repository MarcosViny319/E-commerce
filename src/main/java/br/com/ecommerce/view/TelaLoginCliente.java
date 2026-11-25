package br.com.ecommerce.view;

import br.com.ecommerce.model.SessaoUsuario;
import br.com.ecommerce.service.SupabaseAuthService;
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
public class TelaLoginCliente extends JFrame {
    private JTextField campoEmail;
    private JPasswordField campoSenha;
    private JButton btnEntrar;
    private JButton btnCadastrar;
    private final Consumer<SessaoUsuario> aoAutenticar;
    public TelaLoginCliente(
            Consumer<SessaoUsuario> aoAutenticar) {
        this.aoAutenticar = aoAutenticar;
        setTitle("E-commerce - Acesso do Cliente");
        setSize(420, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        criarInterface();
    }

    private void criarInterface() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel titulo = new JLabel(
                "ACESSO DO CLIENTE",
                JLabel.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 23));
        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(titulo, gbc);
        gbc.gridy = 1;
        painel.add(new JLabel("E-mail:"), gbc);
        campoEmail = new JTextField(25);
        gbc.gridy = 2;
        painel.add(campoEmail, gbc);
        gbc.gridy = 3;
        painel.add(new JLabel("Senha:"), gbc);
        campoSenha = new JPasswordField(25);
        gbc.gridy = 4;
        painel.add(campoSenha, gbc);
        btnEntrar = new JButton("ENTRAR");
        btnCadastrar = new JButton("CADASTRAR");
        btnEntrar.addActionListener(e -> realizarLogin());
        btnCadastrar.addActionListener(e -> cadastrarCliente());
        JPanel botoes = new JPanel(new GridLayout(1, 2, 10, 0));
        botoes.add(btnEntrar);
        botoes.add(btnCadastrar);
        gbc.gridy = 5;
        painel.add(botoes, gbc);

        add(painel);
    }

    private void realizarLogin() {
        String email = campoEmail.getText().trim();
        char[] caracteres = campoSenha.getPassword();
        if (email.isEmpty() || caracteres.length == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Preencha o e-mail e a senha."
            );
            return;
        }

        String senha = new String(caracteres);
        habilitarBotoes(false);
        SwingWorker<SessaoUsuario, Void> worker =
                new SwingWorker<>() {
            @Override
            protected SessaoUsuario doInBackground()
                    throws Exception {
                SupabaseAuthService auth =
                        new SupabaseAuthService();
                return auth.autenticar(email, senha);
            }

            @Override
            protected void done() {
                try {
                    SessaoUsuario sessao = get();

                    if (!sessao.isCliente()) {
                        JOptionPane.showMessageDialog(
                                TelaLoginCliente.this,
                                "Esta conta não possui perfil de cliente.",
                                "Acesso não permitido",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    JOptionPane.showMessageDialog(
                            TelaLoginCliente.this,
                            "Login realizado com sucesso!"
                    );

                    if (aoAutenticar != null) {
                        aoAutenticar.accept(sessao);
                    }

                    dispose();

                } catch (Exception ex) {
                    Throwable causa = ex.getCause();
                    String mensagem = causa != null
                            ? causa.getMessage()
                            : ex.getMessage();
                    JOptionPane.showMessageDialog(
                            TelaLoginCliente.this,
                            "Falha no login: " + mensagem,
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {

                    campoSenha.setText("");
                    habilitarBotoes(true);
                }
            }
        };

        worker.execute();
    }

    private void cadastrarCliente() {
        String email = campoEmail.getText().trim();
        char[] caracteres = campoSenha.getPassword();
        if (email.isEmpty() || caracteres.length == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Preencha o e-mail e a senha."
            );
            return;
        }

        if (caracteres.length < 6) {
            JOptionPane.showMessageDialog(
                    this,
                    "A senha deve ter pelo menos 6 caracteres."
            );
            return;
        }

        String senha = new String(caracteres);
        habilitarBotoes(false);
        SwingWorker<String, Void> worker =
                new SwingWorker<>() {
            @Override
            protected String doInBackground()
                    throws Exception {
                SupabaseAuthService auth =
                        new SupabaseAuthService();
                return auth.cadastrarCliente(email, senha);
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(
                            TelaLoginCliente.this,
                            "Cadastro realizado!\n\n"
                            + "Se a confirmação de e-mail estiver "
                            + "habilitada no Supabase, confirme seu "
                            + "e-mail antes de entrar.\n\n"
                            + "Depois, utilize seu e-mail e senha "
                            + "para fazer login."
                    );

                    campoSenha.setText("");

                } catch (Exception ex) {
                    Throwable causa = ex.getCause();
                    String mensagem = causa != null
                            ? causa.getMessage()
                            : ex.getMessage();
                    JOptionPane.showMessageDialog(
                            TelaLoginCliente.this,
                            "Falha no cadastro: " + mensagem,
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {

                    habilitarBotoes(true);
                }
            }
        };

        worker.execute();
    }

    private void habilitarBotoes(boolean habilitado) {
        btnEntrar.setEnabled(habilitado);
        btnCadastrar.setEnabled(habilitado);
    }
}
