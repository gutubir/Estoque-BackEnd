package com.estoque.service;

import com.estoque.model.Movimentacao;
import com.estoque.model.Produto;
import com.estoque.model.TipoMovimentacao;
import com.estoque.repository.MovimentacaoRepository;
import com.estoque.repository.ProdutoRepository;

import java.time.LocalDate;
import java.util.List;

public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository,
                               ProdutoRepository produtoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.produtoRepository = produtoRepository;
    }

    /**
     * Registra uma nova movimentação e atualiza o estoque do produto
     */
    public Movimentacao registrarMovimentacao(Long produtoId, 
                                             TipoMovimentacao tipo, 
                                             int quantidade,
                                             LocalDate data) {
        // Buscar produto
        Produto produto = produtoRepository.buscarPorId(produtoId);
        if (produto == null) {
            throw new RuntimeException("Produto não encontrado: " + produtoId);
        }

        // Criar movimentação
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setProduto(produto);
        movimentacao.setTipoMovimentacao(tipo);
        movimentacao.setQuantidadeMovimentada(quantidade);
        movimentacao.setDataMovimentacao(data);

        // Atualizar estoque do produto
        int novoEstoque = produto.getQuantidadeEstoque();
        
        if (tipo == TipoMovimentacao.ENTRADA) {
            novoEstoque += quantidade;
        } else if (tipo == TipoMovimentacao.SAIDA) {
            novoEstoque -= quantidade;
            if (novoEstoque < 0) {
                throw new RuntimeException("Estoque insuficiente para a operação");
            }
        }
        
        produto.setQuantidadeEstoque(novoEstoque);
        produtoRepository.salvar(produto);

        // Salvar movimentação
        return movimentacaoRepository.salvar(movimentacao);
    }

    /**
     * Lista todas as movimentações
     */
    public List<Movimentacao> listarMovimentacoes() {
        return movimentacaoRepository.buscarTodos();
    }

    /**
     * Busca movimentações por produto
     */
    public List<Movimentacao> buscarPorProduto(Long produtoId) {
        // TODO: implementar se necessário
        throw new UnsupportedOperationException("Método ainda não implementado");
    }
}
