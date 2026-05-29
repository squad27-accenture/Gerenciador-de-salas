package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.EquipamentosSala;
import com.squad27.gerenciadorsalas.domain.StatusSala;

import java.util.List;

public record SalaDTO(String nome ,
                      Integer capacidade,
                      StatusSala statusSala,
                      String local,
                      List<EquipamentosSala> equipamentosSala,
                      String cidade,
                      String estado)
{

}
