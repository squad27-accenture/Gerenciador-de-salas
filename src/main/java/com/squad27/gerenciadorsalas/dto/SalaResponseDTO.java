package com.squad27.gerenciadorsalas.dto;

public record SalaResponseDTO(Integer id,
                              String nome,
                              Integer capacidade,
                              String local,
                              String cidade,
                              String estado) {
}
