package br.com.pulseapilib.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "api-pulse")
@Data
public class ApiPulseConfig {
    private String serverUrl;    // URL do API Pulse
    private String accessToken;  // Token recebido no registro
    private String telegramToken;// Token do bot Telegram
    private String telegramUsername; // Username do bot Telegram
    private String chatId;       // ID do chat Telegram

    public boolean isValid() {
        return serverUrl != null && !serverUrl.isEmpty() &&
               accessToken != null && !accessToken.isEmpty() &&
               telegramToken != null && !telegramToken.isEmpty() &&
               telegramUsername != null && !telegramUsername.isEmpty() &&
               chatId != null && !chatId.isEmpty();
    }
}
