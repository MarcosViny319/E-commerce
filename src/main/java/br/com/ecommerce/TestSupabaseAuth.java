package br.com.ecommerce;

import br.com.ecommerce.service.SupabaseAuthService;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class TestSupabaseAuth {

    public static void main(String[] args) {

        String email = JOptionPane.showInputDialog(
                null,
                "Digite o e-mail cadastrado no Supabase:"
        );

        if (email == null || email.trim().isEmpty()) {
            return;
        }

        JPasswordField campoSenha = new JPasswordField();

        int opcao = JOptionPane.showConfirmDialog(
                null,
                campoSenha,
                "Digite sua senha do Supabase:",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcao != JOptionPane.OK_OPTION) {
            return;
        }

        String senha = new String(campoSenha.getPassword());

        try {

            SupabaseAuthService auth =
                    new SupabaseAuthService();

            auth.login(email.trim(), senha);

            JOptionPane.showMessageDialog(
                    null,
                    "Autenticação realizada com sucesso!",
                    "Login",
                    JOptionPane.INFORMATION_MESSAGE
            );

            System.out.println("Login realizado com sucesso!");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Não foi possível realizar o login. "
                    + "Verifique as credenciais e a configuração.",
                    "Erro de autenticação",
                    JOptionPane.ERROR_MESSAGE
            );

            System.out.println("Falha na autenticação.");
            e.printStackTrace();

        }
    }
}
