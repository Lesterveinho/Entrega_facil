package com.deliverytech.delivery_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/*
 DTO que representa o retorno da API pública ViaCEP (https://viacep.com.br).
 Os nomes dos campos seguem exatamente o JSON devolvido pelo serviço,
 assim o Jackson faz o mapeamento automático.

 A anotação @JsonIgnoreProperties evita que a desserialização quebre
 caso o ViaCEP passe a devolver novos campos no futuro.
*/
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Endereço retornado pela consulta de CEP (ViaCEP)")
public class CepResponseDTO {

    @Schema(description = "CEP formatado", example = "01310-100")
    private String cep;

    @Schema(description = "Nome da rua / avenida", example = "Avenida Paulista")
    private String logradouro;

    @Schema(description = "Complemento do logradouro", example = "de 612 a 1510 - lado par")
    private String complemento;

    @Schema(description = "Unidade", example = "")
    private String unidade;

    @Schema(description = "Bairro", example = "Bela Vista")
    private String bairro;

    @Schema(description = "Cidade", example = "São Paulo")
    private String localidade;

    @Schema(description = "Sigla do estado", example = "SP")
    private String uf;

    @Schema(description = "Nome do estado", example = "São Paulo")
    private String estado;

    @Schema(description = "Região do país", example = "Sudeste")
    private String regiao;

    @Schema(description = "Código do município no IBGE", example = "3550308")
    private String ibge;

    @Schema(description = "Código GIA", example = "1004")
    private String gia;

    @Schema(description = "DDD da localidade", example = "11")
    private String ddd;

    @Schema(description = "Código SIAFI", example = "7107")
    private String siafi;

    /*
     O ViaCEP não devolve 404 quando o CEP não existe:
     ele responde 200 com o corpo {"erro": true}.
     Guardamos esse campo para poder tratar o caso no service.
    */
    @JsonProperty(value = "erro", access = JsonProperty.Access.WRITE_ONLY)
    @Schema(hidden = true)
    private String erro;

    // Getters e Setters
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getLocalidade() { return localidade; }
    public void setLocalidade(String localidade) { this.localidade = localidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }

    public String getIbge() { return ibge; }
    public void setIbge(String ibge) { this.ibge = ibge; }

    public String getGia() { return gia; }
    public void setGia(String gia) { this.gia = gia; }

    public String getDdd() { return ddd; }
    public void setDdd(String ddd) { this.ddd = ddd; }

    public String getSiafi() { return siafi; }
    public void setSiafi(String siafi) { this.siafi = siafi; }

    public String getErro() { return erro; }
    public void setErro(String erro) { this.erro = erro; }

    /* Retorna true quando o ViaCEP indicou que o CEP não existe (uso interno) */
    @JsonIgnore
    public boolean isNaoEncontrado() {
        return erro != null && Boolean.parseBoolean(erro);
    }

    /* Monta o endereço em uma única linha, pronto para gravar no cadastro */
    @Schema(description = "Endereço em uma única linha",
            example = "Avenida Paulista, Bela Vista, São Paulo - SP, CEP 01310-100")
    public String getEnderecoCompleto() {
        StringBuilder sb = new StringBuilder();

        if (logradouro != null && !logradouro.isBlank()) {
            sb.append(logradouro);
        }
        if (bairro != null && !bairro.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(bairro);
        }
        if (localidade != null && !localidade.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(localidade);
        }
        if (uf != null && !uf.isBlank()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(uf);
        }
        if (cep != null && !cep.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("CEP ").append(cep);
        }

        return sb.toString();
    }
}
