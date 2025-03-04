package br.com.pulseapilib.client.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import br.com.pulseapilib.client.config.ApiPulseConfig;
import br.com.pulseapilib.client.model.StatusReport;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApiPulseReporter {
    private static final Logger logger = LoggerFactory.getLogger(ApiPulseReporter.class);
    private final ApiPulseConfig config;
    private final RestTemplate restTemplate = new RestTemplate();
    private boolean tokenValidated = false;

    public void reportStatus(String apiUrl, int status, String endpoint) {
        if (isSuccessStatus(status) || !isConfigValid()) {
            return;
        }

        validateTokenIfNeeded();
        sendStatusReport(apiUrl, status, endpoint);
    }

    private boolean isSuccessStatus(int status) {
        return status == 200 || status == 201; // 200 OK e 201 Created são sucesso
    }

    private boolean isConfigValid() {
        boolean valid = config.isValid();
        if (!valid) {
            logger.warn("Configurações do API Pulse inválidas. Verifique server-url, access-token, telegram-token, telegram-username e chat-id.");
        }
        return valid;
    }

    private void validateTokenIfNeeded() {
        if (!tokenValidated) {
            logger.info("Validando token de acesso...");
            HttpHeaders headers = createHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);
            try {
                restTemplate.exchange(
                    config.getServerUrl() + "/api/validate?token=" + config.getAccessToken(),
                    HttpMethod.POST,
                    request,
                    Void.class
                );
                logger.info("Token de acesso validado com sucesso!");
                tokenValidated = true;
            } catch (Exception e) {
                logger.error("Falha ao validar token de acesso: {}", e.getMessage());
                // throw new IllegalStateException("Falha ao validar token: " + e.getMessage());
            }
        }
    }

    private void sendStatusReport(String apiUrl, int status, String endpoint) {
        HttpHeaders headers = createHeaders();
        StatusReport report = new StatusReport(apiUrl, status, endpoint, config.getChatId(),config.getAccessToken());
        HttpEntity<StatusReport> request = new HttpEntity<>(report, headers);
        sendReport(request);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getAccessToken());
        return headers;
    }

    private void sendReport(HttpEntity<StatusReport> request) {
        try {
            restTemplate.exchange(config.getServerUrl()+"/api/report", HttpMethod.POST, request, Void.class);
            logger.info("Relatório enviado com sucesso para {} com status {}", request.getBody().getApiUrl(), request.getBody().getStatus());
        } catch (Exception e) {
            logger.error("Erro ao enviar relatório: {}", e.getMessage());
        }
    }
}
