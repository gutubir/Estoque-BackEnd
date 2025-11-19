package com.estoque.model;

import java.time.LocalDate;

public class Movimentacao {

    private Long id;
    private Produto produto;
    private LocalDate dataMovimentacao;
    private int quantidadeMovimentada;
    private TipoMovimentacao tipoMovimentacao;

    public Movimentacao() {}

    public Movimentacao(Long id,
                        Produto produto,
                        LocalDate dataMovimentacao,
                        int quantidadeMovimentada,
                        TipoMovimentacao tipoMovimentacao) {
        this.id = id;
        this.produto = produto;
        this.dataMovimentacao = dataMovimentacao;
        this.quantidadeMovimentada = quantidadeMovimentada;
        this.tipoMovimentacao = tipoMovimentacao;
    }

}
