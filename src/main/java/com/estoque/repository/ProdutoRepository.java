package com.estoque.repository;

import main.java.com.estoque.model.Produto;

import java.util.*;

public class ProdutoRepository {

    private final Map<Long, Produto> banco = new HashMap<>();
    private long sequenciaId = 1L;

    public Produto salvar(Produto produto) {
        if (produto.getId() == null) {
            produto.setId(sequenciaId++);
        }
        banco.put(produto.getId(), produto);
        return produto;
    }

    public Optional<Produto> buscarPorId(Long id) {
        return Optional.ofNullable(banco.get(id));
    }

    public List<Produto> buscarTodos() {
        return new ArrayList<>(banco.values());
    }

    public void remover(Long id) {
        banco.remove(id);
    }
}
