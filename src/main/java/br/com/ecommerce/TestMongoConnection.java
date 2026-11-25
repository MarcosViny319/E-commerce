package br.com.ecommerce;

import br.com.ecommerce.conexao.MongoConnection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class TestMongoConnection {

    public static void main(String[] args) {

        String uri = System.getenv("MONGODB_URI");

        System.out.println("Variavel MONGODB_URI carregada: "
                + (uri != null && !uri.isEmpty()));

        try {
            MongoDatabase database =
                    MongoConnection.getDatabase();

            System.out.println("Conexao realizada com sucesso!");

            MongoCollection<Document> produtos =
                    database.getCollection("produtos");

            System.out.println("Colecao acessada: produtos");

            System.out.println("\n--- PRODUTOS CADASTRADOS ---");

            for (Document produto : produtos.find()) {
                System.out.println(produto.toJson());
            }

        } catch (Exception e) {

            System.out.println("Erro na conexao:");
            e.printStackTrace();

        } finally {
            MongoConnection.closeConnection();
        }
    }
}
