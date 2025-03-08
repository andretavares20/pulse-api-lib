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
    private String accessToken;
    private String telegramToken;
    private String telegramUsername;
    private String chatId;

    /**
     * Verifica se todas as propriedades obrigatórias estão preenchidas.
     */
    public boolean isValid() {
        return isPresent(serverUrl) &&
               isPresent(accessToken) &&
               isPresent(telegramToken) &&
               isPresent(telegramUsername) &&
               isPresent(chatId);
    }

    private boolean isPresent(String value) {
        return value != null && !value.isEmpty();
    }
}
