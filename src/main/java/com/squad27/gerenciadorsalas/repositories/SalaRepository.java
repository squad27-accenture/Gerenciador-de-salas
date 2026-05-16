package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Salas;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaRepository extends JpaRepository<Salas, Integer>{
}
