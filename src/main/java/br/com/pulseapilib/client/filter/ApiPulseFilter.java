package br.com.pulseapilib.client.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import br.com.pulseapilib.client.reporter.ApiPulseReporter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
public class ApiPulseFilter extends OncePerRequestFilter {
    private final ApiPulseReporter reporter;
    private final String apiUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        chain.doFilter(request, response);
        int status = response.getStatus();
        String endpoint = request.getRequestURI();
        reportIfException(status, endpoint);
    }

    private void reportIfException(int status, String endpoint) {
        if (status != 200) {
            reporter.reportStatus(apiUrl, status, endpoint);
        }
    }
}
