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

@Configuration
@EnableConfigurationProperties(ApiPulseConfig.class)
@RequiredArgsConstructor
public class ApiPulseClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiPulseClient.class);
    private final ApiPulseConfig config;

    @Bean
    public ApiPulseReporter apiPulseReporter() {
        logger.info("Criando reporter...");
        return new ApiPulseReporter(config);
    }

    @Bean
    public FilterRegistrationBean<ApiPulseFilter> apiPulseFilter(ApiPulseReporter reporter) {
        logger.info("Registrando filtro...");
        FilterRegistrationBean<ApiPulseFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiPulseFilter(reporter, config.getServerUrl()));
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
