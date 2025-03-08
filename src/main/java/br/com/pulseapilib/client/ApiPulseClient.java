package br.com.pulseapilib.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.pulseapilib.client.config.ApiPulseConfig;
import br.com.pulseapilib.client.filter.ApiPulseFilter;
import br.com.pulseapilib.client.reporter.ApiPulseReporter;
import lombok.RequiredArgsConstructor;

/**
 * Configuração do cliente API Pulse, registrando o reporter e o filtro.
 */
@Configuration
@EnableConfigurationProperties(ApiPulseConfig.class)
@RequiredArgsConstructor
public class ApiPulseClient {

    private static final Logger logger = LoggerFactory.getLogger(ApiPulseClient.class);
    private static final String ALL_URL_PATTERNS = "/*";

    private final ApiPulseConfig apiPulseConfig;

    /**
     * Cria o reporter para envio de status ao API Pulse.
     */
    @Bean
    public ApiPulseReporter apiPulseReporter() {
        logger.info("Criando reporter...");
        return new ApiPulseReporter(apiPulseConfig);
    }

    /**
     * Registra o filtro para interceptar requisições HTTP e reportar falhas.
     */
    @Bean
    public FilterRegistrationBean<ApiPulseFilter> apiPulseFilter(ApiPulseReporter reporter) {
        logger.info("Registrando filtro...");
        return createFilterRegistration(reporter);
    }

    private FilterRegistrationBean<ApiPulseFilter> createFilterRegistration(ApiPulseReporter reporter) {
        FilterRegistrationBean<ApiPulseFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiPulseFilter(reporter, apiPulseConfig.getServerUrl()));
        registration.addUrlPatterns(ALL_URL_PATTERNS);
        return registration;
    }
}
