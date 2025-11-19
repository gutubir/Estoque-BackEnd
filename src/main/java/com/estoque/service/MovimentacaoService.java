package com.estoque.service;

import main.java.com.estoque.model.Movimentacao;
import main.java.com.estoque.model.Produto;
import main.java.com.estoque.model.TipoMovimentacao;
import main.java.com.estoque.repository.MovimentacaoRepository;
import main.java.com.estoque.repository.ProdutoRepository;
import java.util.List;


public class MovimentacaoService {

    private final MovimentacaoRepository movRepository;
    private final ProdutoRepository produtoRepository;

    public MovimentacaoService(MovimentacaoRepository movRepository,
                               ProdutoRepository produtoRepository) {
        this.movRepository = movRepository;
        this.produtoRepository = produtoRepository;
    }

    public Movimentacao registrarMovimentacao(Movimentacao movimentacao) {
        Produto produto = movimentacao.getProduto();

        int qtdAtual = produto.getQuantidadeEstoque();
        int qtdMov = movimentacao.getQuantidadeMovimentada();

        if (movimentacao.getTipoMovimentacao() == TipoMovimentacao.ENTRADA) {
            produto.setQuantidadeEstoque(qtdAtual + qtdMov);
        } else {
            produto.setQuantidadeEstoque(qtdAtual - qtdMov);
        }

        produtoRepository.salvar(produto);
        Movimentacao salva = movRepository.salvar(movimentacao);

        verificarLimites(produto);

        return salva;
    }

    private void verificarLimites(Produto produto) {
        if (produto.getQuantidadeEstoque() < produto.getQuantidadeMinima()) {
            // aqui você pode lançar exceção, logar ou retornar algum aviso
            System.out.println("ATENÇÃO: produto abaixo da quantidade mínima: " + produto.getNome());
        }

        if (produto.getQuantidadeEstoque() > produto.getQuantidadeMaxima()) {
            System.out.println("ATENÇÃO: produto acima da quantidade máxima: " + produto.getNome());
        }
    }

    public List<Movimentacao> listarMovimentacoes() {
        return movRepository.buscarTodos();
    }
}
