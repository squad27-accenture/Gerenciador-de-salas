package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.EquipamentosSala;
import com.squad27.gerenciadorsalas.domain.StatusSala;

public record SalaDTO(String nome ,
                      Integer capacidade,
                      StatusSala statusSala,
                      String local,
                      EquipamentosSala equipamentosSala,
                      String cidade,
                      String estado)
{

}
