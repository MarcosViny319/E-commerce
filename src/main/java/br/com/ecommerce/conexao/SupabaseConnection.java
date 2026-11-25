package br.com.ecommerce.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SupabaseConnection {
    public static Connection conectar() throws SQLException {
        String host = System.getenv("SUPABASE_DB_HOST");
        String port = System.getenv("SUPABASE_DB_PORT");
        String database = System.getenv("SUPABASE_DB_NAME");
        String usuario = System.getenv("SUPABASE_DB_USER");
        String senha = System.getenv("SUPABASE_DB_PASSWORD");
        if (host == null || port == null
                || database == null || usuario == null
                || senha == null) {
            throw new IllegalStateException(
                "Configure as variáveis de ambiente do Supabase."
            );
        }

        String url = "jdbc:postgresql://" + host + ":"
                + port + "/" + database
                + "?sslmode=require";
        return DriverManager.getConnection(
                url, usuario, senha
        );
    }
}
