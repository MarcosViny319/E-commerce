package br.com.ecommerce.conexao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {

    private static MongoClient mongoClient;

    public static MongoDatabase getDatabase() {

        if (mongoClient == null) {

            String uri = System.getenv("MONGODB_URI");

            if (uri == null || uri.isEmpty()) {
                throw new RuntimeException(
                    "Variável de ambiente MONGODB_URI não configurada."
                );
            }

            mongoClient = MongoClients.create(uri);
        }

        return mongoClient.getDatabase("ecommerce_db");
    }

    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }
}
