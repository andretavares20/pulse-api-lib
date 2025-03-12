package br.com.pulseapilib.client.reporter;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private static final String AUTHENTICATE_ENDPOINT = "/api/login"; // Endpoint de autenticação

    private final ApiPulseConfig apiPulseConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private boolean isTokenValidated = false;
    private String accessToken; // Token dinâmico obtido via autenticação

    /**
     * Reporta o status de uma API se indicar falha e a configuração for válida.
     */
    public void reportStatus(String apiUrl, int statusCode, String endpoint) {
        if (isSuccessStatus(statusCode) || !isConfigurationValid()) {
            return;
        }

        authenticateIfNeeded(); // Autentica e obtém o token
        sendStatusReport(apiUrl, statusCode, endpoint);
    }

    private boolean isSuccessStatus(int statusCode) {
        return statusCode == 200 || statusCode == 201; // 200 OK e 201 Created são considerados sucesso
    }

    private boolean isConfigurationValid() {
        boolean isValid = apiPulseConfig.isValid();
        if (!isValid) {
            logger.warn("Configurações do API Pulse inválidas. Verifique server-url, email, password, telegram-token, telegram-username e chat-id.");
        }
        return isValid;
    }

    private void authenticateIfNeeded() {
        if (accessToken == null) {
            logger.info("Autenticando com email e password...");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Criar o corpo da requisição em formato JSON
            Map<String, String> authRequest = new HashMap<>();
            authRequest.put("email", apiPulseConfig.getEmail());
            authRequest.put("password", apiPulseConfig.getPassword());

            HttpEntity<Map<String, String>> request = new HttpEntity<>(authRequest, headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiPulseConfig.getServerUrl() + AUTHENTICATE_ENDPOINT,
                        HttpMethod.POST,
                        request,
                        Map.class
                );
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    accessToken = (String) response.getBody().get("access_token"); // Assume que o token está em "access_token"
                    apiPulseConfig.setAccessToken(accessToken); // Opcional: armazena no config
                    logger.info("Autenticação bem-sucedida, token obtido: {}", accessToken);
                    isTokenValidated = true;
                } else {
                    throw new RuntimeException("Autenticação falhou: " + response.getStatusCode());
                }
            } catch (Exception e) {
                logger.error("Falha ao autenticar: {}", e.getMessage());
                throw new IllegalStateException("Falha ao autenticar com o servidor: " + e.getMessage());
            }
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
                accessToken // Usa o token obtido via autenticação
        );
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (accessToken != null) {
            headers.set("Authorization", "Bearer " + accessToken);
        } else {
            logger.warn("Token de acesso não disponível. Autenticação pode falhar.");
        }
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
