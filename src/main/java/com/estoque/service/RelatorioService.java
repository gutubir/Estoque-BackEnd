package com.estoque.service;

import main.java.com.estoque.model.Produto;
import main.java.com.estoque.model.Movimentacao;
import main.java.com.estoque.repository.MovimentacaoRepository;
import main.java.com.estoque.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class RelatorioService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public RelatorioService(ProdutoRepository produtoRepository,
                            MovimentacaoRepository movimentacaoRepository) {
        this.produtoRepository = produtoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    // 1) Lista de preços
    public List<Produto> gerarListaPrecos() {
        return produtoRepository.buscarTodos().stream()
                .sorted(Comparator.comparing(Produto::getNome))
                .collect(Collectors.toList());
    }

    // 2) Balanço físico-financeiro: para cada produto, quantidade e valor total
    public BigDecimal calcularValorTotalEstoque() {
        return produtoRepository.buscarTodos().stream()
                .map(p -> p.getPrecoUnitario()
                        .multiply(BigDecimal.valueOf(p.getQuantidadeEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 3) Produtos abaixo da quantidade mínima
    public List<Produto> listarProdutosAbaixoMinimo() {
        return produtoRepository.buscarTodos().stream()
                .filter(p -> p.getQuantidadeEstoque() < p.getQuantidadeMinima())
                .collect(Collectors.toList());
    }

    // outros métodos para:
    // - quantidade de produtos por categoria
    // - produto que mais teve saída
    // - produto que mais teve entrada
}