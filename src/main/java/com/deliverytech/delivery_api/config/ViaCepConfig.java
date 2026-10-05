package com.deliverytech.delivery_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/*
 Configuração do cliente HTTP usado para falar com a API pública ViaCEP.

 Deixar a URL base e os timeouts em um bean separado permite
 trocar o endereço do serviço por propriedade (útil em testes)
 sem mexer no código do service.
*/
@Configuration
public class ViaCepConfig {

    @Value("${viacep.base-url:https://viacep.com.br/ws}")
    private String baseUrl;

    @Value("${viacep.connect-timeout-ms:3000}")
    private long connectTimeoutMs;

    @Value("${viacep.read-timeout-ms:5000}")
    private long readTimeoutMs;

    @Bean("viaCepRestClient")
    public RestClient viaCepRestClient() {

        /*
         Timeout é obrigatório em chamada para serviço externo:
         sem ele uma indisponibilidade do ViaCEP travaria as
         threads da nossa API esperando indefinidamente.
        */
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(connectTimeoutMs))
            .build();

        JdkClientHttpRequestFactory requestFactory =
            new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
