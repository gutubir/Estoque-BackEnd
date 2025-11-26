package com.estoque.service;

import com.estoque.model.Categoria;
import com.estoque.model.Movimentacao;
import com.estoque.model.Produto;
import com.estoque.model.TipoMovimentacao;
import com.estoque.repository.MovimentacaoRepository;
import com.estoque.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RelatorioService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public RelatorioService(ProdutoRepository produtoRepository,
                            MovimentacaoRepository movimentacaoRepository) {
        this.produtoRepository = produtoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    // 1) Lista de preços com categoria
    public List<Produto> gerarListaPrecos() {
        // ProdutoRepository.buscarTodos() já traz categoria (id/nome)
        return produtoRepository.buscarTodos();
    }

    // 2) Balanço físico/financeiro por produto
    //    (pode usar no backend ou só ficar como apoio para o handler)
    public List<Produto> gerarBalancoFisicoFinanceiro() {
        return produtoRepository.buscarTodos();
        // o cálculo valorTotal = preco * quantidade
        // pode ser feito no handler/JSON do HttpApiServer
    }

    // 3) Produtos abaixo da quantidade mínima
    public List<Produto> listarProdutosAbaixoDoMinimo() {
        return produtoRepository.buscarTodos().stream()
                .filter(p -> p.getQuantidadeEstoque() < p.getQuantidadeMinima())
                .sorted(Comparator.comparing(Produto::getNome))
                .collect(Collectors.toList());
    }

    // 4) Quantidade de produtos por categoria
    public Map<String, Long> contarProdutosPorCategoria() {
        return produtoRepository.buscarTodos().stream()
                .collect(Collectors.groupingBy(
                        p -> {
                            Categoria c = p.getCategoria();
                            return c != null && c.getNome() != null
                                    ? c.getNome()
                                    : "Sem categoria";
                        },
                        Collectors.counting()
                ));
    }

    // 5) Produto com maior saída e maior entrada
    public MovimentacaoResumoMovimento calcularMaiorEntradaESaida() {
        List<Movimentacao> movs = movimentacaoRepository.buscarTodos();

        Map<Long, Integer> totalEntradaPorProduto = movs.stream()
                .filter(m -> m.getTipoMovimentacao() == TipoMovimentacao.ENTRADA)
                .collect(Collectors.groupingBy(
                        m -> m.getProduto().getId(),
                        Collectors.summingInt(Movimentacao::getQuantidadeMovimentada)
                ));

        Map<Long, Integer> totalSaidaPorProduto = movs.stream()
                .filter(m -> m.getTipoMovimentacao() == TipoMovimentacao.SAIDA)
                .collect(Collectors.groupingBy(
                        m -> m.getProduto().getId(),
                        Collectors.summingInt(Movimentacao::getQuantidadeMovimentada)
                ));

        Long produtoMaisEntradaId = totalEntradaPorProduto.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Long produtoMaisSaidaId = totalSaidaPorProduto.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Produto produtoMaisEntrada = produtoMaisEntradaId != null
                ? produtoRepository.buscarPorId(produtoMaisEntradaId)
                : null;

        Produto produtoMaisSaida = produtoMaisSaidaId != null
                ? produtoRepository.buscarPorId(produtoMaisSaidaId)
                : null;

        Integer qtdMaisEntrada = produtoMaisEntradaId != null
                ? totalEntradaPorProduto.get(produtoMaisEntradaId)
                : 0;

        Integer qtdMaisSaida = produtoMaisSaidaId != null
                ? totalSaidaPorProduto.get(produtoMaisSaidaId)
                : 0;

        return new MovimentacaoResumoMovimento(produtoMaisEntrada, qtdMaisEntrada,
                                               produtoMaisSaida, qtdMaisSaida);
    }

    // já existia: valor total do estoque
    public BigDecimal calcularValorTotalEstoque() {
        return produtoRepository.buscarTodos().stream()
                .map(p -> p.getPrecoUnitario()
                        .multiply(BigDecimal.valueOf(p.getQuantidadeEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // DTO simples para retornar no endpoint de maior entrada/saída
    public static class MovimentacaoResumoMovimento {
        private final Produto produtoMaisEntrada;
        private final int quantidadeEntrada;
        private final Produto produtoMaisSaida;
        private final int quantidadeSaida;

        public MovimentacaoResumoMovimento(Produto produtoMaisEntrada, int quantidadeEntrada,
                                           Produto produtoMaisSaida, int quantidadeSaida) {
            this.produtoMaisEntrada = produtoMaisEntrada;
            this.quantidadeEntrada = quantidadeEntrada;
            this.produtoMaisSaida = produtoMaisSaida;
            this.quantidadeSaida = quantidadeSaida;
        }

        public Produto getProdutoMaisEntrada() {
            return produtoMaisEntrada;
        }

        public int getQuantidadeEntrada() {
            return quantidadeEntrada;
        }

        public Produto getProdutoMaisSaida() {
            return produtoMaisSaida;
        }

        public int getQuantidadeSaida() {
            return quantidadeSaida;
        }
    }
}
