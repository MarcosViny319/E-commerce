package br.com.ecommerce.model;

public class SessaoUsuario {
    private final String idUsuario;
    private final String email;
    private final String accessToken;
    private final String refreshToken;
    private final String papel;
    public SessaoUsuario(
            String idUsuario,
            String email,
            String accessToken,
            String refreshToken,
            String papel) {
        this.idUsuario = idUsuario;
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.papel = papel;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getEmail() {
        return email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getPapel() {
        return papel;
    }

    public boolean isAdmin() {
        return "admin".equals(papel);
    }

    public boolean isCliente() {
        return "cliente".equals(papel);
    }
}
