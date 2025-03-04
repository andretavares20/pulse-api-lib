package br.com.pulseapilib.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor
public class StatusReport {
    private String apiUrl;
    private int status;
    private String endpoint;
    private String chatId;
    private String accessToken;
}
