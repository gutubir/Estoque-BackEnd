package com.estoque.service;

import com.estoque.model.Produto;
import com.estoque.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.util.List;

public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    // CRUD
    public Produto criarProduto(Produto produto) {
        return produtoRepository.salvar(produto);
    }

    public Produto atualizarProduto(Produto produto) {
        return produtoRepository.salvar(produto);
    }

    public void removerProduto(Long id) {
        produtoRepository.remover(id);
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.buscarTodos();
    }

    //Reajusta o preço de todos os produtos em um percentual
    public void reajustarPrecos(BigDecimal percentual) {
        for (Produto p : produtoRepository.buscarTodos()) {
            BigDecimal novoPreco = p.getPrecoUnitario()
                    .multiply(BigDecimal.ONE.add(percentual));
            p.setPrecoUnitario(novoPreco);
            produtoRepository.salvar(p);
        }
    }

}