package com.squad27.gerenciadorsalas;

import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.enums.EquipamentosAssento;
import com.squad27.gerenciadorsalas.enums.StatusSala;
import com.squad27.gerenciadorsalas.enums.TipoFuncionario;
import com.squad27.gerenciadorsalas.repositories.AssentoRepository;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.enums.Role;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import com.squad27.gerenciadorsalas.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AssentoIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired SalaRepository salaRepository;
    @Autowired AssentoRepository assentoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired TokenService tokenService;
    @Autowired PasswordEncoder passwordEncoder;

    Sala sala;
    String token;

    @BeforeEach
    void setup() {
        sala = new Sala();
        sala.setNome("Sala Assento Test");
        sala.setCapacidade(3);
        sala.setDeletado(false);
        sala.setStatus(StatusSala.DISPONIVEL);
        sala = salaRepository.save(sala);

        Assento a1 = new Assento();
        a1.setSala(sala);
        a1.setPosicao(1);
        a1.setTipoAssento("ESTACAO_PADRAO");
        a1.setCoordenadaX(10.5);
        a1.setCoordenadaY(20.0);
        a1.setAtivo(true);
        a1.setEquipamentos(List.of(EquipamentosAssento.MONITOR, EquipamentosAssento.COMPUTADOR_PC));

        Assento a2 = new Assento();
        a2.setSala(sala);
        a2.setPosicao(2);
        a2.setTipoAssento("POSICAO_ACESSIVEL");
        a2.setCoordenadaX(15.0);
        a2.setCoordenadaY(20.0);
        a2.setAtivo(false);
        a2.setEquipamentos(List.of());

        Assento a3 = new Assento();
        a3.setSala(sala);
        a3.setPosicao(3);
        a3.setAtivo(true);

        assentoRepository.saveAll(List.of(a1, a2, a3));

        Usuario user = new Usuario("user@test.com", passwordEncoder.encode("123"), Role.USER, "Tester", TipoFuncionario.PROGRAMADOR);
        user = usuarioRepository.save(user);
        token = "Bearer " + tokenService.generateToken(user);
    }

    @Test
    @DisplayName("Listar assentos: retorna todos com novos campos")
    void listarAssentos_retornaNovoCampos() throws Exception {
        mockMvc.perform(get("/api/v1/salas/" + sala.getId() + "/assentos")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].tipoAssento").value("ESTACAO_PADRAO"))
                .andExpect(jsonPath("$[0].coordenadaX").value(10.5))
                .andExpect(jsonPath("$[0].coordenadaY").value(20.0))
                .andExpect(jsonPath("$[0].ativo").value(true))
                .andExpect(jsonPath("$[0].equipamentos.length()").value(2))
                .andExpect(jsonPath("$[1].ativo").value(false))
                .andExpect(jsonPath("$[1].tipoAssento").value("POSICAO_ACESSIVEL"))
                .andExpect(jsonPath("$[2].tipoAssento").isEmpty())
                .andExpect(jsonPath("$[2].coordenadaX").isEmpty())
                .andExpect(jsonPath("$[2].ativo").value(true));
    }

    @Test
    @DisplayName("Listar assentos: sala inexistente retorna lista vazia")
    void listarAssentos_salaInexistente_retornaListaVazia() throws Exception {
        mockMvc.perform(get("/api/v1/salas/99999/assentos")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}