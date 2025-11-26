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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public LocalDate getDataMovimentacao() {
        return dataMovimentacao;
    }

    public void setDataMovimentacao(LocalDate dataMovimentacao) {
        this.dataMovimentacao = dataMovimentacao;
    }

    public int getQuantidadeMovimentada() {
        return quantidadeMovimentada;
    }

    public void setQuantidadeMovimentada(int quantidadeMovimentada) {
        this.quantidadeMovimentada = quantidadeMovimentada;
    }

    public TipoMovimentacao getTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public void setTipoMovimentacao(TipoMovimentacao tipoMovimentacao) {
        this.tipoMovimentacao = tipoMovimentacao;
    }

}
