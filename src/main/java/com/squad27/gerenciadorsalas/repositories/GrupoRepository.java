package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Integer> {

    List<Grupo> findAllByAtivoTrue();

    Optional<Grupo> findByIdAndAtivoTrue(Integer id);

    @Query("""
        select distinct g
        from Grupo g
        left join g.usuarios u
        left join fetch g.lider
        where g.ativo = true
        and (
            g.lider.email = :email
            or u.email = :email
        )
    """)
    List<Grupo> buscarMeusGrupos(@Param("email") String email);
}