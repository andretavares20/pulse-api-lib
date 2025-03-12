package br.com.pulseapilib.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuração do API Pulse mapeada a partir das propriedades com prefixo 'api-pulse'.
 */
@ConfigurationProperties(prefix = "api-pulse")
@Data
public class ApiPulseConfig {

    private String serverUrl;
    private String email;
    private String password;
    private String telegramToken;
    private String telegramUsername;
    private String chatId;

    /**
     * Verifica se todas as propriedades obrigatórias estão preenchidas.
     */
    public boolean isValid() {
        return isPresent(serverUrl) &&
               isPresent(email) &&
               isPresent(password) &&
               isPresent(telegramToken) &&
               isPresent(telegramUsername) &&
               isPresent(chatId);
    }

    private boolean isPresent(String value) {
        return value != null && !value.isEmpty();
    }

    // Método para limpar o token, se necessário (opcional)
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
