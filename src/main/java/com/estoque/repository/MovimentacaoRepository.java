package com.estoque.repository;

import com.estoque.config.DatabaseConnection;
import com.estoque.model.Movimentacao;
import com.estoque.model.Produto;
import com.estoque.model.TipoMovimentacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimentacaoRepository {

    public Movimentacao salvar(Movimentacao mov) {
        String sql;
        if (mov.getId() == null) {
            sql = "INSERT INTO movimentacoes " +
                  "(produto_id, data_movimentacao, quantidade, tipo) " +
                  "VALUES (?, ?, ?, ?) RETURNING id";
        } else {
            sql = "UPDATE movimentacoes SET produto_id = ?, data_movimentacao = ?, " +
                  "quantidade_movimentada = ?, tipo_movimentacao = ? " +
                  "WHERE id = ? RETURNING id";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, mov.getProduto().getId());
            stmt.setDate(2, Date.valueOf(mov.getDataMovimentacao()));
            stmt.setInt(3, mov.getQuantidadeMovimentada());
            stmt.setObject(4, mov.getTipoMovimentacao(), Types.OTHER);

            if (mov.getId() != null) {
                stmt.setLong(5, mov.getId());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    mov.setId(rs.getLong("id"));
                }
            }
            return mov;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar movimentação", e);
        }
    }

    public List<Movimentacao> buscarTodos() {
        String sql = """
            SELECT m.*, p.nome AS produto_nome
            FROM movimentacoes m
            JOIN produtos p ON p.id = m.produto_id
            ORDER BY m.data_movimentacao DESC, m.id DESC
            """;

        List<Movimentacao> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar movimentações", e);
        }
    }

    public List<Movimentacao> buscarPorProduto(Long produtoId) {
        String sql = """
            SELECT m.*, p.nome AS produto_nome
            FROM movimentacoes m
            JOIN produtos p ON p.id = m.produto_id
            WHERE m.produto_id = ?
            ORDER BY m.data_movimentacao DESC, m.id DESC
            """;

        List<Movimentacao> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, produtoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar movimentações por produto", e);
        }
    }

    private Movimentacao mapResultSet(ResultSet rs) throws SQLException {
        Movimentacao m = new Movimentacao();
        m.setId(rs.getLong("id"));
        m.setDataMovimentacao(rs.getDate("data_movimentacao").toLocalDate());
        m.setQuantidadeMovimentada(rs.getInt("quantidade"));
        m.setTipoMovimentacao(TipoMovimentacao.valueOf(rs.getString("tipo")));

        Produto p = new Produto();
        p.setId(rs.getLong("produto_id"));
        p.setNome(rs.getString("produto_nome"));
        m.setProduto(p);

        return m;
    }
}
