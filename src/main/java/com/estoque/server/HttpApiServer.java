package com.estoque.server;

import com.estoque.model.Categoria;
import com.estoque.model.Movimentacao;
import com.estoque.model.Produto;
import com.estoque.model.TipoMovimentacao;
import com.estoque.service.CategoriaService;
import com.estoque.service.MovimentacaoService;
import com.estoque.service.ProdutoService;
import com.estoque.service.RelatorioService;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpApiServer {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final MovimentacaoService movimentacaoService;
    private final RelatorioService relatorioService;
    private final int port;

    public HttpApiServer(ProdutoService produtoService,
                         CategoriaService categoriaService,
                         MovimentacaoService movimentacaoService,
                         RelatorioService relatorioService,
                         int port) {
        this.produtoService = produtoService;
        this.categoriaService = categoriaService;
        this.movimentacaoService = movimentacaoService;
        this.relatorioService = relatorioService;
        this.port = port;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/api/produtos", new ProdutosHandler(produtoService));
        server.createContext("/api/categorias", new CategoriasHandler(categoriaService));
        server.createContext("/api/movimentacoes", new MovimentacoesHandler(movimentacaoService));
        server.createContext("/api/relatorios/precos", new RelatoriosPrecosHandler(relatorioService));
        server.createContext("/api/relatorios/balanco", new RelatoriosBalancoHandler(relatorioService));
        server.createContext("/api/relatorios/abaixo-minimo", new RelatoriosAbaixoMinimoHandler(relatorioService));
        server.createContext("/api/relatorios/produtos-por-categoria", new RelatoriosProdutosPorCategoriaHandler(relatorioService));
        server.createContext("/api/relatorios/movimentacoes-top", new RelatoriosMovimentacoesTopHandler(relatorioService));


        server.setExecutor(null);
        server.start();
        System.out.println("HTTP API ouvindo em http://0.0.0.0:" + port);
    }

    // ==================== MÉTODOS AUXILIARES COMPARTILHADOS ====================
    static String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining());
        }
    }

    static String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return "";

        start += pattern.length();
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length()) return "";

        char firstChar = json.charAt(start);

        if (firstChar == '"') {
            start++;
            int end = json.indexOf('"', start);
            return end != -1 ? json.substring(start, end) : "";
        } else {
            int end = start;
            while (end < json.length() &&
                    json.charAt(end) != ',' &&
                    json.charAt(end) != '}' &&
                    json.charAt(end) != ' ') {
                end++;
            }
            return json.substring(start, end).trim();
        }
    }

    static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        try {
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception ignored) {
        }
    }

    static void addCorsHeaders(Headers headers) {
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
    }

    // ==================== HANDLER DE PRODUTOS ====================
    static class ProdutosHandler implements HttpHandler {
        private final ProdutoService produtoService;

        public ProdutosHandler(ProdutoService produtoService) {
            this.produtoService = produtoService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod().toUpperCase();
                String path = exchange.getRequestURI().getPath();

                addCorsHeaders(exchange.getResponseHeaders());

                if ("OPTIONS".equals(method)) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("GET".equals(method)) {
                    handleGet(exchange);
                } else if ("POST".equals(method)) {
                    handlePost(exchange);
                } else if ("PUT".equals(method)) {
                    handlePut(exchange, path);
                } else if ("DELETE".equals(method)) {
                    handleDelete(exchange, path);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Erro interno no servidor");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            List<Produto> produtos = produtoService.listarProdutos();
            String json = toJsonArray(produtos);
            sendJsonResponse(exchange, 200, json);
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);
            Produto produto = parseProduto(body);
            Produto criado = produtoService.criarProduto(produto);
            String json = toJson(criado);
            sendJsonResponse(exchange, 201, json);
        }

        private void handlePut(HttpExchange exchange, String path) throws IOException {
            String[] parts = path.split("/");
            if (parts.length < 4) {
                sendError(exchange, 400, "ID não fornecido");
                return;
            }

            Long id = Long.parseLong(parts[3]);
            String body = readBody(exchange);
            Produto produto = parseProduto(body);
            produto.setId(id);

            Produto atualizado = produtoService.atualizarProduto(produto);
            String json = toJson(atualizado);
            sendJsonResponse(exchange, 200, json);
        }

        private void handleDelete(HttpExchange exchange, String path) throws IOException {
            String[] parts = path.split("/");
            if (parts.length < 4) {
                sendError(exchange, 400, "ID não fornecido");
                return;
            }

            Long id = Long.parseLong(parts[3]);
            produtoService.removerProduto(id);
            exchange.sendResponseHeaders(204, -1);
        }

        private Produto parseProduto(String json) {
            Produto p = new Produto();
            p.setNome(extractValue(json, "nome"));
            p.setPrecoUnitario(new BigDecimal(extractValue(json, "precoUnitario")));
            p.setUnidade(extractValue(json, "unidade"));
            p.setQuantidadeEstoque(Integer.parseInt(extractValue(json, "quantidadeEstoque")));
            p.setQuantidadeMinima(Integer.parseInt(extractValue(json, "quantidadeMinima")));
            p.setQuantidadeMaxima(Integer.parseInt(extractValue(json, "quantidadeMaxima")));

            // novo: categoriaId vindo do front
            String categoriaIdStr = extractValue(json, "categoriaId");
            if (!categoriaIdStr.isEmpty()) {
                Categoria c = new Categoria();
                c.setId(Long.parseLong(categoriaIdStr));
                p.setCategoria(c);
        }

        return p;
        }


        private String toJsonArray(List<Produto> produtos) {
            return produtos.stream()
                    .map(this::toJson)
                    .collect(Collectors.joining(",", "[", "]"));
        }

        private String toJson(Produto p) {
        Long categoriaId = p.getCategoria() != null ? p.getCategoria().getId() : null;
        String categoriaNome = (p.getCategoria() != null && p.getCategoria().getNome() != null)
                ? p.getCategoria().getNome() : "";

        return String.format(
            "{\"id\":%d,\"nome\":\"%s\",\"precoUnitario\":%s,\"unidade\":\"%s\"," +
                "\"quantidadeEstoque\":%d,\"quantidadeMinima\":%d,\"quantidadeMaxima\":%d," +
                "\"categoriaId\":%s,\"categoriaNome\":\"%s\"}",
            p.getId(),
            escape(p.getNome()),
            p.getPrecoUnitario(),
            escape(p.getUnidade()),
            p.getQuantidadeEstoque(),
            p.getQuantidadeMinima(),
            p.getQuantidadeMaxima(),
            categoriaId != null ? categoriaId.toString() : "null",
            escape(categoriaNome)
        );
        }

    }

    // ==================== HANDLER DE CATEGORIAS ====================
    static class CategoriasHandler implements HttpHandler {
        private final CategoriaService categoriaService;

        public CategoriasHandler(CategoriaService categoriaService) {
            this.categoriaService = categoriaService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod().toUpperCase();
                String path = exchange.getRequestURI().getPath();

                addCorsHeaders(exchange.getResponseHeaders());

                if ("OPTIONS".equals(method)) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("GET".equals(method)) {
                    handleGet(exchange);
                } else if ("POST".equals(method)) {
                    handlePost(exchange);
                } else if ("PUT".equals(method)) {
                    handlePut(exchange, path);
                } else if ("DELETE".equals(method)) {
                    handleDelete(exchange, path);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Erro interno no servidor");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            List<Categoria> categorias = categoriaService.listarCategorias();
            String json = toJsonArray(categorias);
            sendJsonResponse(exchange, 200, json);
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);
            Categoria categoria = parseCategoria(body);
            Categoria criada = categoriaService.criarCategoria(categoria);
            String json = toJson(criada);
            sendJsonResponse(exchange, 201, json);
        }

        private void handlePut(HttpExchange exchange, String path) throws IOException {
            String[] parts = path.split("/");
            if (parts.length < 4) {
                sendError(exchange, 400, "ID não fornecido");
                return;
            }

            Long id = Long.parseLong(parts[3]);
            String body = readBody(exchange);
            Categoria categoria = parseCategoria(body);
            categoria.setId(id);

            Categoria atualizada = categoriaService.atualizarCategoria(categoria);
            String json = toJson(atualizada);
            sendJsonResponse(exchange, 200, json);
        }

        private void handleDelete(HttpExchange exchange, String path) throws IOException {
            String[] parts = path.split("/");
            if (parts.length < 4) {
                sendError(exchange, 400, "ID não fornecido");
                return;
            }

            Long id = Long.parseLong(parts[3]);
            categoriaService.removerCategoria(id);
            exchange.sendResponseHeaders(204, -1);
        }

        private Categoria parseCategoria(String json) {
            Categoria c = new Categoria();
            c.setNome(extractValue(json, "nome"));
            c.setDescricao(extractValue(json, "descricao"));
            
            c.setTamanho(extractValue(json, "tamanho"));
            c.setEmbalagem(extractValue(json, "embalagem"));
            return c;
        }


        private String toJsonArray(List<Categoria> categorias) {
            return categorias.stream()
                    .map(this::toJson)
                    .collect(Collectors.joining(",", "[", "]"));
        }

        private String toJson(Categoria c) {
            return String.format(
                "{\"id\":%d,\"nome\":\"%s\",\"descricao\":\"%s\",\"tamanho\":\"%s\",\"embalagem\":\"%s\"}",
                c.getId(),
                escape(c.getNome()),
                escape(c.getDescricao() != null ? c.getDescricao() : ""),
                escape(c.getTamanho() != null ? c.getTamanho() : ""),
                escape(c.getEmbalagem() != null ? c.getEmbalagem() : "")
            );
        }

    }

    // ==================== HANDLER DE MOVIMENTAÇÕES ====================
    static class MovimentacoesHandler implements HttpHandler {
        private final MovimentacaoService movimentacaoService;

        public MovimentacoesHandler(MovimentacaoService movimentacaoService) {
            this.movimentacaoService = movimentacaoService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod().toUpperCase();

                addCorsHeaders(exchange.getResponseHeaders());

                if ("OPTIONS".equals(method)) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("GET".equals(method)) {
                    handleGet(exchange);
                } else if ("POST".equals(method)) {
                    handlePost(exchange);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Erro interno no servidor");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            List<Movimentacao> movimentacoes = movimentacaoService.listarMovimentacoes();
            String json = toJsonArray(movimentacoes);
            sendJsonResponse(exchange, 200, json);
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);

            Long produtoId = Long.parseLong(extractValue(body, "produtoId"));
            String tipoStr = extractValue(body, "tipo");
            int quantidade = Integer.parseInt(extractValue(body, "quantidade"));
            String dataStr = extractValue(body, "data");

            TipoMovimentacao tipo = TipoMovimentacao.valueOf(tipoStr);
            LocalDate data = LocalDate.parse(dataStr);

            Movimentacao criada = movimentacaoService.registrarMovimentacao(
                    produtoId, tipo, quantidade, data
            );

            String json = toJson(criada);
            sendJsonResponse(exchange, 201, json);
        }

        private String toJsonArray(List<Movimentacao> movimentacoes) {
            return movimentacoes.stream()
                    .map(this::toJson)
                    .collect(Collectors.joining(",", "[", "]"));
        }

        private String toJson(Movimentacao m) {
            return String.format(
                    "{\"id\":%d,\"produtoId\":%d,\"tipo\":\"%s\",\"quantidade\":%d,\"data\":\"%s\"}",
                    m.getId(),
                    m.getProduto().getId(),
                    m.getTipoMovimentacao(),
                    m.getQuantidadeMovimentada(),
                    m.getDataMovimentacao()
            );
        }
    }

    // ==================== HANDLER DE RELATÓRIOS - PREÇOS ====================
    static class RelatoriosPrecosHandler implements HttpHandler {
        private final RelatorioService relatorioService;

        public RelatoriosPrecosHandler(RelatorioService relatorioService) {
            this.relatorioService = relatorioService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                addCorsHeaders(exchange.getResponseHeaders());

                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                List<Produto> produtos = relatorioService.gerarListaPrecos();

                String json = produtos.stream()
                        .map(p -> String.format(
                                "{" +
                                "\"id\":%d," +
                                "\"nome\":\"%s\"," +
                                "\"precoUnitario\":%s," +
                                "\"unidade\":\"%s\"," +
                                "\"quantidadeEstoque\":%d," +
                                "\"categoriaNome\":\"%s\"" +
                                "}",
                                p.getId(),
                                escape(p.getNome()),
                                p.getPrecoUnitario(),
                                escape(p.getUnidade()),
                                p.getQuantidadeEstoque(),
                                escape(p.getCategoria() != null && p.getCategoria().getNome() != null
                                        ? p.getCategoria().getNome()
                                        : "")
                        ))
                        .collect(Collectors.joining(",", "[", "]"));

                sendJsonResponse(exchange, 200, json);
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Erro interno no servidor");
            }
        }
    }

    // ==================== HANDLER DE RELATÓRIOS - BALANÇO ====================
    static class RelatoriosBalancoHandler implements HttpHandler {
        private final RelatorioService relatorioService;

        public RelatoriosBalancoHandler(RelatorioService relatorioService) {
            this.relatorioService = relatorioService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                addCorsHeaders(exchange.getResponseHeaders());

                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                BigDecimal valorTotal = relatorioService.calcularValorTotalEstoque();
                String json = String.format("{\"valorTotal\":%s}", valorTotal);

                sendJsonResponse(exchange, 200, json);
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Erro interno no servidor");
            }
        }
    }

    static class RelatoriosAbaixoMinimoHandler implements HttpHandler {
        private final RelatorioService relatorioService;

        public RelatoriosAbaixoMinimoHandler(RelatorioService relatorioService) {
            this.relatorioService = relatorioService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                addCorsHeaders(exchange.getResponseHeaders());

                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                List<Produto> produtos = relatorioService.listarProdutosAbaixoDoMinimo();

                String json = produtos.stream()
                        .map(p -> String.format(
                                "{" +
                                "\"id\":%d," +
                                "\"nome\":\"%s\"," +
                                "\"quantidadeEstoque\":%d," +
                                "\"quantidadeMinima\":%d" +
                                "}",
                                p.getId(),
                                escape(p.getNome()),
                                p.getQuantidadeEstoque(),
                                p.getQuantidadeMinima()
                        ))
                        .collect(Collectors.joining(",", "[", "]"));

                sendJsonResponse(exchange, 200, json);
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Erro interno no servidor");
            }
        }
    }

    static class RelatoriosProdutosPorCategoriaHandler implements HttpHandler {
        private final RelatorioService relatorioService;

        public RelatoriosProdutosPorCategoriaHandler(RelatorioService relatorioService) {
            this.relatorioService = relatorioService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                addCorsHeaders(exchange.getResponseHeaders());

                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                Map<String, Long> mapa = relatorioService.contarProdutosPorCategoria();

                String json = mapa.entrySet().stream()
                        .map(e -> String.format(
                                "{" +
                                "\"categoria\":\"%s\"," +
                                "\"quantidade\":%d" +
                                "}",
                                escape(e.getKey()),
                                e.getValue()
                        ))
                        .collect(Collectors.joining(",", "[", "]"));

                sendJsonResponse(exchange, 200, json);
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Erro interno no servidor");
            }
        }
    }

    static class RelatoriosMovimentacoesTopHandler implements HttpHandler {
        private final RelatorioService relatorioService;

        public RelatoriosMovimentacoesTopHandler(RelatorioService relatorioService) {
            this.relatorioService = relatorioService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                addCorsHeaders(exchange.getResponseHeaders());

                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                RelatorioService.MovimentacaoResumoMovimento resumo =
                        relatorioService.calcularMaiorEntradaESaida();

                Produto pEntrada = resumo.getProdutoMaisEntrada();
                Produto pSaida   = resumo.getProdutoMaisSaida();

                String json = String.format(
                        "{" +
                        "\"maisEntrada\":{" +
                            "\"produtoId\":%s," +
                            "\"nome\":\"%s\"," +
                            "\"quantidade\":%d" +
                        "}," +
                        "\"maisSaida\":{" +
                            "\"produtoId\":%s," +
                            "\"nome\":\"%s\"," +
                            "\"quantidade\":%d" +
                        "}" +
                        "}",
                        pEntrada != null ? pEntrada.getId() : null,
                        escape(pEntrada != null ? pEntrada.getNome() : ""),
                        resumo.getQuantidadeEntrada(),
                        pSaida != null ? pSaida.getId() : null,
                        escape(pSaida != null ? pSaida.getNome() : ""),
                        resumo.getQuantidadeSaida()
                );

                sendJsonResponse(exchange, 200, json);
            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Erro interno no servidor");
            }
        }
    }
}
