package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.ApiResponseWrapper;
import com.deliverytech.delivery_api.dto.CepResponseDTO;
import com.deliverytech.delivery_api.service.CepService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
 Controller REST que expõe a consulta de endereço por CEP.

 A integração em si (chamada HTTP, tratamento de erro) fica no CepService:
 o controller só recebe a requisição e devolve a resposta padronizada,
 mantendo a separação de camadas usada no restante do projeto.
*/
@RestController
@RequestMapping("/api/cep")
@CrossOrigin(origins = "*")
@Tag(name = "CEP", description = "Consulta de endereços na API pública ViaCEP")
public class BuscaCepController {

    // injetando o serviço de consulta de CEP
    @Autowired
    private CepService cepService;

    /*
     Busca o endereço completo de um CEP.
     Aceita com ou sem máscara: /api/cep/01310-100 ou /api/cep/01310100
    */
    @GetMapping("/{cep}")
    @Operation(summary = "Buscar endereço por CEP",
               description = "Consulta a API pública ViaCEP e retorna o endereço do CEP informado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereço encontrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "CEP inválido ou serviço indisponível"),
        @ApiResponse(responseCode = "404", description = "CEP não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<CepResponseDTO>> buscarPorCep(
            @Parameter(description = "CEP a ser consultado, com ou sem máscara", example = "01310-100")
            @PathVariable("cep") String cep) {

        CepResponseDTO endereco = cepService.buscarPorCep(cep);

        ApiResponseWrapper<CepResponseDTO> response =
            new ApiResponseWrapper<>(true, endereco, "Endereço encontrado com sucesso");

        return ResponseEntity.ok(response);
    }

    /*
     Variação que devolve somente o endereço em uma única linha,
     pronto para preencher o campo "endereco" do cadastro de cliente.
    */
    @GetMapping("/{cep}/endereco")
    @Operation(summary = "Buscar endereço formatado por CEP",
               description = "Retorna o endereço do CEP em uma única linha, pronto para o cadastro")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereço encontrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "CEP inválido ou serviço indisponível"),
        @ApiResponse(responseCode = "404", description = "CEP não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<Map<String, String>>> buscarEnderecoFormatado(
            @Parameter(description = "CEP a ser consultado, com ou sem máscara", example = "01310-100")
            @PathVariable("cep") String cep) {

        String enderecoFormatado = cepService.buscarEnderecoFormatado(cep);

        ApiResponseWrapper<Map<String, String>> response = new ApiResponseWrapper<>(
            true,
            Map.of("cep", cep, "endereco", enderecoFormatado),
            "Endereço encontrado com sucesso"
        );

        return ResponseEntity.ok(response);
    }
}
