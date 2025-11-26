-- Tipo ENUM para movimentações
CREATE TYPE IF NOT EXISTS tipo_movimentacao AS ENUM ('ENTRADA', 'SAIDA');

-- Tabela de categorias
CREATE TABLE IF NOT EXISTS categorias (
    id         SERIAL PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    descricao  TEXT,
    tamanho    VARCHAR(20)  NOT NULL,   -- Pequeno, Médio, Grande
    embalagem  VARCHAR(20)  NOT NULL    -- Lata, Vidro, Plástico
);

-- Tabela de produtos
CREATE TABLE IF NOT EXISTS produtos (
    id                  SERIAL PRIMARY KEY,
    nome                VARCHAR(100)   NOT NULL,
    preco_unitario      DECIMAL(10, 2) NOT NULL,
    unidade             VARCHAR(10)    NOT NULL,
    quantidade_estoque  INTEGER        NOT NULL DEFAULT 0,
    quantidade_minima   INTEGER        NOT NULL DEFAULT 0,
    quantidade_maxima   INTEGER        NOT NULL DEFAULT 0,
    categoria_id        BIGINT REFERENCES categorias(id)
);

-- Tabela de movimentações
CREATE TABLE IF NOT EXISTS movimentacoes (
    id                     SERIAL PRIMARY KEY,
    produto_id             BIGINT           NOT NULL REFERENCES produtos(id),
    data_movimentacao      DATE             NOT NULL,
    quantidade_movimentada INTEGER          NOT NULL,
    tipo_movimentacao      tipo_movimentacao NOT NULL
);

-- Dados iniciais de categorias
INSERT INTO categorias (nome, descricao, tamanho, embalagem) VALUES 
    ('Eletrônicos', 'Produtos eletrônicos em geral', 'Médio',  'Plástico'),
    ('Alimentos',   'Produtos alimentícios',         'Médio',  'Lata'),
    ('Limpeza',     'Produtos de limpeza',           'Grande', 'Plástico'),
    ('Papelaria',   'Materiais de escritório',       'Pequeno','Plástico')
ON CONFLICT DO NOTHING;

-- Produto de exemplo
INSERT INTO produtos 
    (nome, preco_unitario, unidade, quantidade_estoque, quantidade_minima, quantidade_maxima, categoria_id)
VALUES 
    ('Notebook Dell', 3500.00, 'UN', 10, 2, 50, 1)
ON CONFLICT DO NOTHING;
