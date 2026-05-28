package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
}
