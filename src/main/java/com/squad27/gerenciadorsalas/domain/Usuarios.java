package com.squad27.gerenciadorsalas.domain;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usuarios")
@Entity
@EqualsAndHashCode(of = "id")
public class Usuarios implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String email;
    private String senha;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String username;

    public Usuarios(String email, String senha, Role role, String username){
        this.email = email;
        this.senha = senha;
        this.role = role;
        this.username = username;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        if (this.role == Role.ADMIN)
            return List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_TECHLEADER")
        );
        if(this.role == Role.TECHLEADER)
            return List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_TECHLEADER")
            );
        else
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

}
