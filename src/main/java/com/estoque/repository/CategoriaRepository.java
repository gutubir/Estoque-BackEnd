package com.estoque.repository;

import com.estoque.config.DatabaseConnection;
import com.estoque.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaRepository {

    public Categoria salvar(Categoria categoria) {
        String sql;
        if (categoria.getId() == null) {
            sql = "INSERT INTO categorias (nome, descricao, tamanho, embalagem) " +
                  "VALUES (?, ?, ?, ?) RETURNING id";
        } else {
            sql = "UPDATE categorias SET nome = ?, descricao = ?, tamanho = ?, embalagem = ? " +
                  "WHERE id = ? RETURNING id";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getDescricao());
            stmt.setString(3, categoria.getTamanho());
            stmt.setString(4, categoria.getEmbalagem());

            if (categoria.getId() != null) {
                stmt.setLong(5, categoria.getId());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    categoria.setId(rs.getLong("id"));
                }
            }
            return categoria;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar categoria", e);
        }
    }

    public void remover(Long id) {
        String sql = "DELETE FROM categorias WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover categoria", e);
        }
    }

    public Categoria buscarPorId(Long id) {
        String sql = "SELECT id, nome, descricao, tamanho, embalagem " +
                     "FROM categorias WHERE id = ?";

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
            throw new RuntimeException("Erro ao buscar categoria", e);
        }
    }

    public List<Categoria> buscarTodos() {
        String sql = "SELECT id, nome, descricao, tamanho, embalagem " +
                     "FROM categorias ORDER BY nome";

        List<Categoria> categorias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categorias.add(mapResultSet(rs));
            }

            return categorias;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar categorias", e);
        }
    }

    private Categoria mapResultSet(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getLong("id"));
        c.setNome(rs.getString("nome"));
        c.setDescricao(rs.getString("descricao"));
        c.setTamanho(rs.getString("tamanho"));
        c.setEmbalagem(rs.getString("embalagem"));
        return c;
    }
}
