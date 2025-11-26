package com.estoque.repository;

import com.estoque.config.DatabaseConnection;
import com.estoque.model.Categoria;
import com.estoque.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepository {

    public Produto salvar(Produto produto) {
        String sql;
        if (produto.getId() == null) {
            sql = "INSERT INTO produtos (nome, preco_unitario, unidade, quantidade_estoque, " +
                    "quantidade_minima, quantidade_maxima, categoria_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        } else {
            sql = "UPDATE produtos SET nome = ?, preco_unitario = ?, unidade = ?, quantidade_estoque = ?, " +
                    "quantidade_minima = ?, quantidade_maxima = ?, categoria_id = ? " +
                    "WHERE id = ? RETURNING id";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, produto.getNome());
            stmt.setBigDecimal(2, produto.getPrecoUnitario());
            stmt.setString(3, produto.getUnidade());
            stmt.setInt(4, produto.getQuantidadeEstoque());
            stmt.setInt(5, produto.getQuantidadeMinima());
            stmt.setInt(6, produto.getQuantidadeMaxima());

            if (produto.getCategoria() != null) {
                stmt.setLong(7, produto.getCategoria().getId());
            } else {
                stmt.setNull(7, Types.BIGINT);
            }

            if (produto.getId() != null) {
                stmt.setLong(8, produto.getId());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    produto.setId(rs.getLong("id"));
                }
            }
            return produto;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar produto", e);
        }
    }

    public void remover(Long id) {
        String sql = "DELETE FROM produtos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover produto", e);
        }
    }

    public Produto buscarPorId(Long id) {
        String sql = """
                SELECT p.*, c.nome AS categoria_nome
                FROM produtos p
                LEFT JOIN categorias c ON c.id = p.categoria_id
                WHERE p.id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto", e);
        }
    }

    public List<Produto> buscarTodos() {
        String sql = """
                SELECT p.*, c.nome AS categoria_nome
                FROM produtos p
                LEFT JOIN categorias c ON c.id = p.categoria_id
                ORDER BY p.nome
                """;

        List<Produto> produtos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                produtos.add(mapResultSet(rs));
            }

            return produtos;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produtos", e);
        }
    }

    private Produto mapResultSet(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getLong("id"));
        p.setNome(rs.getString("nome"));
        p.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
        p.setUnidade(rs.getString("unidade"));
        p.setQuantidadeEstoque(rs.getInt("quantidade_estoque"));
        p.setQuantidadeMinima(rs.getInt("quantidade_minima"));
        p.setQuantidadeMaxima(rs.getInt("quantidade_maxima"));

        Long categoriaId = rs.getLong("categoria_id");
        if (!rs.wasNull()) {
            Categoria c = new Categoria();
            c.setId(categoriaId);
            // vem do alias no SELECT
            c.setNome(rs.getString("categoria_nome"));
            p.setCategoria(c);
        }

        return p;
    }
}
