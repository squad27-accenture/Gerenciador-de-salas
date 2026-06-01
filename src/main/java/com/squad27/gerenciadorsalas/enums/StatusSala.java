package com.squad27.gerenciadorsalas.enums;

public enum StatusSala {
    DISPONIVEL(1),
    INDISPONIVEL(2),
    MANUTENCAO(3);

    private int codigo;

    StatusSala(int codigo){
        this.codigo = codigo;
    }

    public int getCodigo(){
        return codigo;
    }
}
