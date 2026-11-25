package br.com.ecommerce;

import br.com.ecommerce.conexao.SupabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class TesteSupabaseConnection {

    public static void main(String[] args) {

        System.out.println("================================");
        System.out.println("   TESTE DE CONEXAO SUPABASE");
        System.out.println("================================");

        try (Connection conexao =
                SupabaseConnection.conectar()) {

            System.out.println(
                "Conexao realizada com sucesso!"
            );

            System.out.println(
                "Banco de dados: " + conexao.getCatalog()
            );

            System.out.println(
                "Status: conectado"
            );

        } catch (SQLException | IllegalStateException e) {

            System.out.println(
                "Falha na conexao com Supabase!"
            );

            System.out.println(
                "Motivo: " + e.getMessage()
            );
        }
    }
}
