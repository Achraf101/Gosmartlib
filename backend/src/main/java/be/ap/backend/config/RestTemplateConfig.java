package be.ap.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;

/**
 * Configuratie van HTTP-gerelateerde beans.
 */
@Configuration
@EnableConfigurationProperties(EncryptionProperties.class)
public class RestTemplateConfig {

    /**
     * Maakt een {@link RestTemplate} aan met een geconfigureerde Apache HTTP-client
     * en expliciete time-outs voor uitgaande HTTP-aanroepen.
     *
     * @return de geconfigureerde {@link RestTemplate}
     */
    @Bean
    public RestTemplate restTemplate() {
        CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectionRequestTimeout(Timeout.ofSeconds(3))
                                .setResponseTimeout(Timeout.ofSeconds(5))
                                .build())
                .build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
    }
}