package br.com.ecommerce.service;

import br.com.ecommerce.model.SessaoUsuario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class SupabaseAuthService {
    private final String supabaseUrl;
    private final String publishableKey;
    private final HttpClient client;
    private final ObjectMapper mapper;
    public SupabaseAuthService() {
        String url = System.getenv("SUPABASE_URL");
        publishableKey = System.getenv("SUPABASE_PUBLISHABLE_KEY");
        if (url == null || url.isBlank()
                || publishableKey == null || publishableKey.isBlank()) {
            throw new IllegalStateException(
                    "Configure SUPABASE_URL e SUPABASE_PUBLISHABLE_KEY."
            );
        }
        supabaseUrl = url.replaceAll("/+$", "");
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        mapper = new ObjectMapper();
    }

    public String login(String email, String senha) throws Exception {
        String url = supabaseUrl
                + "/auth/v1/token?grant_type=password";
        String corpo = mapper.createObjectNode()
                .put("email", email)
                .put("password", senha)
                .toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("apikey", publishableKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 200) {
            return response.body();
        }

        throw new RuntimeException(
                "Falha na autenticação. HTTP "
                + response.statusCode() + " - " + response.body()
        );
    }

    public SessaoUsuario autenticar(String email, String senha)
            throws Exception {
        String resposta = login(email, senha);
        JsonNode dados = mapper.readTree(resposta);
        String accessToken = dados.path("access_token").asText();
        String refreshToken = dados.path("refresh_token").asText();
        String idUsuario = dados.path("user").path("id").asText();
        String emailUsuario = dados.path("user").path("email").asText();
        if (accessToken.isBlank() || idUsuario.isBlank()) {
            throw new RuntimeException(
                    "Não foi possível obter os dados da sessão."
            );
        }
        String papel = consultarPapel(idUsuario, accessToken);
        if (papel == null || papel.isBlank()) {
            throw new RuntimeException(
                    "Perfil do usuário não encontrado."
            );
        }
        return new SessaoUsuario(
                idUsuario,
                emailUsuario,
                accessToken,
                refreshToken,
                papel
        );
    }

    private String consultarPapel(String userId, String accessToken)
            throws Exception {
        String idCodificado = URLEncoder.encode(
                userId, StandardCharsets.UTF_8
        );
        String url = supabaseUrl
                + "/rest/v1/perfis?id=eq."
                + idCodificado
                + "&select=papel";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("apikey", publishableKey)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Falha ao consultar perfil. HTTP "
                    + response.statusCode()
                    + " - " + response.body()
            );
        }
        JsonNode perfis = mapper.readTree(response.body());
        if (!perfis.isArray() || perfis.isEmpty()) {
            return null;
        }
        return perfis.get(0).path("papel").asText();
    }

    public String cadastrarCliente(String email, String senha)
            throws Exception {
        String url = supabaseUrl + "/auth/v1/signup";
        String corpo = mapper.createObjectNode()
                .put("email", email)
                .put("password", senha)
                .toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("apikey", publishableKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 200
                || response.statusCode() == 201) {
            return response.body();
        }

        throw new RuntimeException(
                "Falha no cadastro. HTTP "
                + response.statusCode() + " - " + response.body()
        );
    }

    public boolean loginComoAdmin(String email, String senha)
            throws Exception {
        SessaoUsuario sessao = autenticar(email, senha);
        return sessao.isAdmin();
    }
}
