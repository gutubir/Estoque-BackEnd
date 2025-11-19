package com.estoque.model;

public class Categoria {

    private Long id;
    private String nome;    
    private String tamanho; // "Pequeno", "Médio", "Grande"
    private String embalagem; // "Lata", "Vidro", "Plástico"

    public Categoria() {}

    public Categoria(Long id, String nome, String tamanho, String embalagem) {
        this.id = id;
        this.nome = nome;
        this.tamanho = tamanho;
        this.embalagem = embalagem;
    }

}