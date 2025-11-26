package com.estoque.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    // NÃO guardar conexão estática
    // private static Connection connection = null;
    
    public static Connection getConnection() {
        try {
            // URL padrão pensada para rodar em Docker (serviço 'db')
            String url = System.getenv("DATABASE_URL") != null
                    ? System.getenv("DATABASE_URL")
                    : "jdbc:postgresql://db:5432/estoque";

            String user = System.getenv("DB_USER") != null
                    ? System.getenv("DB_USER")
                    : "admin";

            String password = System.getenv("DB_PASSWORD") != null
                    ? System.getenv("DB_PASSWORD")
                    : "admin123";

            // Registra o driver (só precisa fazer uma vez, mas não faz mal chamar sempre)
            Class.forName("org.postgresql.Driver");

            // SEMPRE cria uma nova conexão
            Connection conn = DriverManager.getConnection(url, user, password);
            
            System.out.println("✓ Nova conexão com banco de dados estabelecida!");
            
            return conn;

        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL não encontrado!");
            e.printStackTrace();
            throw new RuntimeException("Driver PostgreSQL não encontrado", e);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados!");
            e.printStackTrace();
            throw new RuntimeException("Erro ao conectar ao banco de dados", e);
        }
    }
    
    // Não precisa mais de closeConnection global
    // Cada repository deve fechar sua própria conexão no try-with-resources
}
