package com.estoque;

import com.estoque.repository.CategoriaRepository;
import com.estoque.repository.MovimentacaoRepository;
import com.estoque.repository.ProdutoRepository;
import com.estoque.service.CategoriaService;
import com.estoque.service.MovimentacaoService;
import com.estoque.service.ProdutoService;
import com.estoque.service.RelatorioService;
import com.estoque.server.HttpApiServer;

public class Main {

    public static void main(String[] args) throws Exception {
        // Repositories
        ProdutoRepository produtoRepository = new ProdutoRepository();
        CategoriaRepository categoriaRepository = new CategoriaRepository();
        MovimentacaoRepository movimentacaoRepository = new MovimentacaoRepository();

        // Services
        ProdutoService produtoService = new ProdutoService(produtoRepository);
        CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        MovimentacaoService movimentacaoService = new MovimentacaoService(
            movimentacaoRepository, produtoRepository
        );
        RelatorioService relatorioService = new RelatorioService(
            produtoRepository, movimentacaoRepository
        );

        // HTTP Server
        HttpApiServer apiServer = new HttpApiServer(
            produtoService,
            categoriaService,
            movimentacaoService,
            relatorioService,
            5000
        );
        
        apiServer.start();
    }
}
