package br.com.ecommerce.dao;

import br.com.ecommerce.conexao.MongoConnection;
import br.com.ecommerce.model.Produto;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class ProdutoDAO {
    private MongoCollection<Document> getColecao() {
        MongoDatabase database = MongoConnection.getDatabase();
        return database.getCollection("produtos");
    }

    public void cadastrar(Produto produto) {
        Document documento = new Document()
                .append("nome", produto.getNome())
                .append("descricao", produto.getDescricao())
                .append("preco", produto.getPreco())
                .append("categoria", produto.getCategoria())
                .append("imagem", produto.getImagem())
                .append("estoque", produto.getEstoque())
                .append("ativo", produto.isAtivo());
        getColecao().insertOne(documento);
        produto.setId(documento.getObjectId("_id"));
    }

    private Produto converterDocumento(Document documento) {
        Produto produto = new Produto();
        produto.setId(documento.getObjectId("_id"));
        produto.setNome(documento.getString("nome"));
        produto.setDescricao(documento.getString("descricao"));
        Number preco = documento.get("preco", Number.class);
        produto.setPreco(preco != null ? preco.doubleValue() : 0);
        produto.setCategoria(documento.getString("categoria"));
        produto.setImagem(documento.getString("imagem"));
        Number estoque = documento.get("estoque", Number.class);
        produto.setEstoque(estoque != null ? estoque.intValue() : 0);
        produto.setAtivo(Boolean.TRUE.equals(
                documento.getBoolean("ativo")
        ));
        return produto;
    }

    public List<Produto> listar() {
        List<Produto> lista = new ArrayList<>();
        for (Document documento : getColecao().find()) {
            lista.add(converterDocumento(documento));
        }
        return lista;
    }

    public Produto buscarPorId(ObjectId id) {
        if (id == null) {
            return null;
        }

        Document documento = getColecao()
                .find(new Document("_id", id))
                .first();
        if (documento == null) {
            return null;
        }
        return converterDocumento(documento);
    }

    public boolean atualizar(Produto produto) {
        if (produto == null || produto.getId() == null) {
            return false;
        }
        Document atualizacao = new Document()
                .append("nome", produto.getNome())
                .append("descricao", produto.getDescricao())
                .append("preco", produto.getPreco())
                .append("categoria", produto.getCategoria())
                .append("imagem", produto.getImagem())
                .append("estoque", produto.getEstoque())
                .append("ativo", produto.isAtivo());
        UpdateResult resultado = getColecao().updateOne(
                new Document("_id", produto.getId()),
                new Document("$set", atualizacao)
        );
        return resultado.getMatchedCount() > 0;
    }

    public boolean excluir(ObjectId id) {
        if (id == null) {
            return false;
        }

        DeleteResult resultado = getColecao().deleteOne(
                new Document("_id", id)
        );

        return resultado.getDeletedCount() > 0;
    }
    public boolean reporEstoque(ObjectId id, int quantidade) {

        if (id == null || quantidade <= 0) {
            return false;
        }

        UpdateResult resultado = getColecao().updateOne(
                Filters.eq("_id", id),
                Updates.inc("estoque", quantidade)
        );

        return resultado.getMatchedCount() == 1;
    }

    public boolean reduzirEstoque(ObjectId id, int quantidade) {

        if (id == null || quantidade <= 0) {
            return false;
        }

        UpdateResult resultado = getColecao().updateOne(
                Filters.and(
                        Filters.eq("_id", id),
                        Filters.gte("estoque", quantidade)
                ),
                Updates.inc("estoque", -quantidade)
        );

        return resultado.getMatchedCount() == 1;
    }
}
