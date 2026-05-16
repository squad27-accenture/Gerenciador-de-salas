package com.squad27.gerenciadorsalas.domain;

public enum Role {
    ADMIN(1),
    USER(2),
    TECHLEADER(3);

    private int codigo;

    Role(int codigo){
        this.codigo = codigo;
    }

    public int getCodigo(){
        return codigo;
    }
}
