package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.CepResponseDTO;

/*
 Contrato da camada de serviço responsável pela integração
 com a API pública ViaCEP.
 A implementação fica em service.impl.CepServiceImpl
*/
public interface CepService {

    /*
     Consulta um CEP no ViaCEP.
     Aceita o CEP com ou sem máscara (ex: "01310-100" ou "01310100").
     Lança BusinessException para CEP inválido ou serviço indisponível
     e EntityNotFoundException quando o CEP não existe.
    */
    CepResponseDTO buscarPorCep(String cep);

    /*
     Consulta o endereço e devolve apenas o texto pronto
     para ser gravado no campo "endereco" dos cadastros.
    */
    String buscarEnderecoFormatado(String cep);
}
