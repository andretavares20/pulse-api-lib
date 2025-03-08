package br.com.pulseapilib.client.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import br.com.pulseapilib.client.reporter.ApiPulseReporter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filtro que intercepta requisições HTTP e reporta falhas ao API Pulse.
 */
@RequiredArgsConstructor
public class ApiPulseFilter extends OncePerRequestFilter {

    private final ApiPulseReporter apiPulseReporter;
    private final String apiUrl;

    /**
     * Processa a requisição e reporta o status se houver falha.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
        int statusCode = response.getStatus();
        String endpoint = request.getRequestURI();
        reportFailureIfNeeded(statusCode, endpoint);
    }

    private void reportFailureIfNeeded(int statusCode, String endpoint) {
        if (isFailureStatus(statusCode)) {
            apiPulseReporter.reportStatus(apiUrl, statusCode, endpoint);
        }
    }

    private boolean isFailureStatus(int statusCode) {
        return statusCode != 200 && statusCode != 201; 
    }
}
