package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.CepResponseDTO;
import com.deliverytech.delivery_api.exception.BusinessException;
import com.deliverytech.delivery_api.exception.EntityNotFoundException;
import com.deliverytech.delivery_api.service.CepService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.regex.Pattern;

/*
 Implementação da integração com a API pública ViaCEP.

 O RestClient já vem configurado com a URL base (ver ViaCepConfig),
 então aqui só montamos o caminho /{cep}/json/.
*/
@Service
public class CepServiceImpl implements CepService {

    private static final Logger log = LoggerFactory.getLogger(CepServiceImpl.class);

    /* O ViaCEP só aceita CEP com exatamente 8 dígitos */
    private static final Pattern CEP_SOMENTE_DIGITOS = Pattern.compile("^[0-9]{8}$");

    private final RestClient viaCepRestClient;

    public CepServiceImpl(@Qualifier("viaCepRestClient") RestClient viaCepRestClient) {
        this.viaCepRestClient = viaCepRestClient;
    }

    @Override
    public CepResponseDTO buscarPorCep(String cep) {

        String cepLimpo = normalizar(cep);

        log.info("Consultando o CEP {} na API ViaCEP", cepLimpo);

        CepResponseDTO endereco;

        try {
            endereco = viaCepRestClient.get()
                .uri("/{cep}/json/", cepLimpo)
                .retrieve()
                .body(CepResponseDTO.class);

        } catch (HttpClientErrorException e) {
            /*
             O ViaCEP devolve 400 quando o CEP tem formato que ele não aceita.
             Isso é erro do dado enviado, não indisponibilidade do serviço.
            */
            log.warn("ViaCEP rejeitou o CEP {}: {}", cepLimpo, e.getStatusCode());
            throw new BusinessException(
                "CEP inválido: '" + cep + "'",
                "CEP_INVALID",
                e
            );

        } catch (RestClientException e) {
            /*
             Qualquer falha de comunicação (timeout, DNS, 5xx do ViaCEP)
             cai aqui. Não faz sentido devolver 500 genérico para o cliente,
             então traduzimos para uma BusinessException com código próprio.
            */
            log.error("Falha ao consultar o CEP {} no ViaCEP: {}", cepLimpo, e.getMessage());
            throw new BusinessException(
                "Serviço de consulta de CEP indisponível no momento. Tente novamente.",
                "CEP_SERVICE_UNAVAILABLE",
                e
            );
        }

        /*
         Atenção: o ViaCEP responde HTTP 200 com {"erro": true}
         quando o CEP tem formato válido mas não existe.
        */
        if (endereco == null || endereco.isNaoEncontrado()) {
            log.warn("CEP {} não encontrado no ViaCEP", cepLimpo);
            throw new EntityNotFoundException("CEP " + cepLimpo + " não foi encontrado");
        }

        return endereco;
    }

    @Override
    public String buscarEnderecoFormatado(String cep) {
        return buscarPorCep(cep).getEnderecoCompleto();
    }

    /*
     Remove máscara, espaços e valida o formato antes de chamar a API externa.
     Validar aqui evita uma requisição de rede desnecessária.
    */
    private String normalizar(String cep) {

        if (cep == null || cep.isBlank()) {
            throw new BusinessException("CEP é obrigatório", "CEP_INVALID");
        }

        String cepLimpo = cep.replaceAll("[^0-9]", "");

        if (!CEP_SOMENTE_DIGITOS.matcher(cepLimpo).matches()) {
            throw new BusinessException(
                "CEP inválido: '" + cep + "'. Informe 8 dígitos, com ou sem máscara (ex: 01310-100)",
                "CEP_INVALID"
            );
        }

        return cepLimpo;
    }
}
