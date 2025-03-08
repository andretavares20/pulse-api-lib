package br.com.pulseapilib.client.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import br.com.pulseapilib.client.config.ApiPulseConfig;
import br.com.pulseapilib.client.model.StatusReport;
import lombok.RequiredArgsConstructor;

/**
 * Reporta falhas de status de APIs ao servidor do API Pulse.
 */
@RequiredArgsConstructor
public class ApiPulseReporter {

    private static final Logger logger = LoggerFactory.getLogger(ApiPulseReporter.class);
    private static final String REPORT_ENDPOINT = "/api/report";
    private static final String VALIDATE_ENDPOINT = "/api/validate?token=";

    private final ApiPulseConfig apiPulseConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private boolean isTokenValidated = false;

    /**
     * Reporta o status de uma API se indicar falha e a configuração for válida.
     */
    public void reportStatus(String apiUrl, int statusCode, String endpoint) {
        if (isSuccessStatus(statusCode) || !isConfigurationValid()) {
            return;
        }

        validateAccessTokenIfNeeded();
        sendStatusReport(apiUrl, statusCode, endpoint);
    }

    private boolean isSuccessStatus(int statusCode) {
        return statusCode == 200 || statusCode == 201; // 200 OK e 201 Created são considerados sucesso
    }

    private boolean isConfigurationValid() {
        boolean isValid = apiPulseConfig.isValid();
        if (!isValid) {
            logger.warn("Configurações do API Pulse inválidas. Verifique server-url, access-token, telegram-token, telegram-username e chat-id.");
        }
        return isValid;
    }

    private void validateAccessTokenIfNeeded() {
        if (!isTokenValidated) {
            logger.info("Validando token de acesso...");
            HttpHeaders headers = createHttpHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);
            performTokenValidation(request);
        }
    }

    private void performTokenValidation(HttpEntity<String> request) {
        try {
            restTemplate.exchange(
                    apiPulseConfig.getServerUrl() + VALIDATE_ENDPOINT + apiPulseConfig.getAccessToken(),
                    HttpMethod.POST,
                    request,
                    Void.class
            );
            logger.info("Token de acesso validado com sucesso!");
            isTokenValidated = true;
        } catch (Exception e) {
            logger.error("Falha ao validar token de acesso: {}", e.getMessage());
            // throw new IllegalStateException("Falha ao validar token: " + e.getMessage());
        }
    }

    private void sendStatusReport(String apiUrl, int statusCode, String endpoint) {
        HttpHeaders headers = createHttpHeaders();
        StatusReport report = createStatusReport(apiUrl, statusCode, endpoint);
        HttpEntity<StatusReport> request = new HttpEntity<>(report, headers);
        sendReportToServer(request);
    }

    private StatusReport createStatusReport(String apiUrl, int statusCode, String endpoint) {
        return new StatusReport(
                apiUrl,
                statusCode,
                endpoint,
                apiPulseConfig.getChatId(),
                apiPulseConfig.getAccessToken()
        );
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiPulseConfig.getAccessToken());
        return headers;
    }

    private void sendReportToServer(HttpEntity<StatusReport> request) {
        try {
            restTemplate.exchange(
                    apiPulseConfig.getServerUrl() + REPORT_ENDPOINT,
                    HttpMethod.POST,
                    request,
                    Void.class
            );
            logger.info("Relatório enviado com sucesso para {} com status {}",
                    request.getBody().getApiUrl(), request.getBody().getStatusCode());
        } catch (Exception e) {
            logger.error("Erro ao enviar relatório: {}", e.getMessage());
        }
    }
}
