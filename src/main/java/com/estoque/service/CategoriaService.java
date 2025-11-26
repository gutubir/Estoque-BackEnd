package com.estoque.service;

import com.estoque.model.Categoria;
import com.estoque.repository.CategoriaRepository;

import java.util.List;

public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Categoria criarCategoria(Categoria categoria) {
        return categoriaRepository.salvar(categoria);
    }

    public Categoria atualizarCategoria(Categoria categoria) {
        return categoriaRepository.salvar(categoria);
    }

    public void removerCategoria(Long id) {
        categoriaRepository.remover(id);
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.buscarTodos();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.buscarPorId(id);
    }
}
