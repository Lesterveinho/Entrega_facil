package com.deliverytech.delivery_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CalculoPedidoDTO {

    @NotEmpty(message = "A lista de itens não pode estar vazia")
    @Valid
    private List<ItemPedidoDTO> itens;

    // Getters e Setters
    public List<ItemPedidoDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoDTO> itens) {
        this.itens = itens;
    }
}
