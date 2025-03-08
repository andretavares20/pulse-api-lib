package br.com.pulseapilib.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa um relatório de status de uma API para envio ao sistema de monitoramento.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusReport {

    private String apiUrl;
    private int statusCode;
    private String endpoint;
    private String chatId;
    private String accessToken;
}
