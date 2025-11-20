package com.example.demo.repository;

import com.example.demo.model.ImoveisModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImoveisRepository extends JpaRepository<ImoveisModel, Integer> {

    // O Spring Data JPA gera automaticamente os métodos básicos (findAll, findById, save, delete, etc.)

    // Buscar imóveis por ID do usuário proprietário
    List<ImoveisModel> findByUsuarioId(Integer usuarioId);

    // Buscar imóveis por ID do bairro
    List<ImoveisModel> findByBairroId(Integer bairroId);

    // Buscar imóveis por ID do tipo
    List<ImoveisModel> findByTipoImovelId(Integer tipoImovelId);

    // Buscar imóveis por status
    List<ImoveisModel> findByStatus(ImoveisModel.Status status);

    // Buscar imóveis por destaque
    List<ImoveisModel> findByDestaqueTrue();

    // Buscar imóveis por ID do usuário e ID do imóvel (útil para verificar propriedade)
    @Query("SELECT i FROM ImoveisModel i WHERE i.id = :id AND i.usuario.id = :usuarioId")
    ImoveisModel findByIdAndUsuarioId(@Param("id") Integer id, @Param("usuarioId") Integer usuarioId);

    // Buscar imóveis por ID do bairro e incluir dados do bairro e tipo (otimizado)
    @Query("SELECT i FROM ImoveisModel i JOIN FETCH i.bairro b JOIN FETCH i.tipoImovel t WHERE i.bairro.id = :bairroId")
    List<ImoveisModel> findByBairroIdWithBairroAndTipo(@Param("bairroId") Integer bairroId);

    // Buscar imóveis por ID do tipo e incluir dados do bairro e tipo (otimizado)
    @Query("SELECT i FROM ImoveisModel i JOIN FETCH i.bairro b JOIN FETCH i.tipoImovel t WHERE i.tipoImovel.id = :tipoId")
    List<ImoveisModel> findByTipoImovelIdWithBairroAndTipo(@Param("tipoId") Integer tipoId);
}