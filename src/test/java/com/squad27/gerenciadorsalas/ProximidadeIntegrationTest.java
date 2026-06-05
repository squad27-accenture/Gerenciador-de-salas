package com.squad27.gerenciadorsalas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.squad27.gerenciadorsalas.domain.*;
import com.squad27.gerenciadorsalas.dto.ReservaGrupoDTO;
import com.squad27.gerenciadorsalas.enums.*;
import com.squad27.gerenciadorsalas.repositories.*;
import com.squad27.gerenciadorsalas.security.TokenService;
import com.squad27.gerenciadorsalas.services.NotificacaoEmailService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ProximidadeIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired SalaRepository salaRepository;
    @Autowired AssentoRepository assentoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired DisponibilidadeSalaRepository disponibilidadeSalaRepository;
    @Autowired TokenService tokenService;
    @Autowired PasswordEncoder passwordEncoder;
    @MockitoBean NotificacaoEmailService notificacaoEmailService;

    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    Sala sala;
    String token;

    static final LocalDate AMANHA = LocalDate.now().plusDays(1);
    static final LocalTime H9  = LocalTime.of(9, 0);
    static final LocalTime H11 = LocalTime.of(11, 0);

    @BeforeEach
    void setup() {
        sala = new Sala();
        sala.setNome("Sala Proximidade");
        sala.setCapacidade(4);
        sala.setDeletado(false);
        sala.setStatus(StatusSala.DISPONIVEL);
        sala.setRaioProximidade(5.0);
        sala = salaRepository.save(sala);

        for (DiaSemana dia : DiaSemana.values()) {
            DisponibilidadeSala disp = new DisponibilidadeSala();
            disp.setSala(sala);
            disp.setDiaSemana(dia);
            disp.setAceitaReservas(true);
            disp.setHorarioAbertura(LocalTime.of(7, 0));
            disp.setHorarioFechamento(LocalTime.of(22, 0));
            disp.setAntecedenciaMinimaDias(0);
            disponibilidadeSalaRepository.save(disp);
        }

        Assento a1 = assento(sala, 1, "ESTACAO_PADRAO", 1.0, 1.0);
        Assento a2 = assento(sala, 2, "ESTACAO_PADRAO", 2.0, 1.0);
        Assento a3 = assento(sala, 3, "ESTACAO_EXECUTIVA", 10.0, 10.0);
        Assento a4 = assento(sala, 4, "ESTACAO_PADRAO", 20.0, 20.0);
        assentoRepository.saveAll(List.of(a1, a2, a3, a4));

        Usuario user = new Usuario("user@prox.com", passwordEncoder.encode("123"), Role.USER, "Tester", TipoFuncionario.OUTRO);
        user = usuarioRepository.save(user);
        token = "Bearer " + tokenService.generateToken(user);
    }

    @Test
    @DisplayName("Proximidade OBRIGATORIA: assentos próximos disponíveis → confirma")
    void proximidadeObrigatoria_sucesso() throws Exception {
        ReservaGrupoDTO dto = new ReservaGrupoDTO(H9, H11, AMANHA, sala.getId(), 10,
                List.of(List.of("ESTACAO_PADRAO"), List.of("ESTACAO_PADRAO")),
                CriterioProximidade.OBRIGATORIA);

        mockMvc.perform(post("/api/v1/reserva/reservaGrupo")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Proximidade OBRIGATORIA: tipos em assentos distantes → rejeita 409")
    void proximidadeObrigatoria_rejeita_quandoDistantes() throws Exception {
        ReservaGrupoDTO dto = new ReservaGrupoDTO(H9, H11, AMANHA, sala.getId(),10,
                List.of(List.of("ESTACAO_PADRAO"), List.of("ESTACAO_EXECUTIVA")),
                CriterioProximidade.OBRIGATORIA);

        mockMvc.perform(post("/api/v1/reserva/reservaGrupo")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PROXIMIDADE_INSUFICIENTE"));
    }

    @Test
    @DisplayName("Proximidade PREFERENCIAL: aloca mesmo sem proximidade ideal")
    void proximidadePreferencial_alocaMesmoDistante() throws Exception {
        ReservaGrupoDTO dto = new ReservaGrupoDTO(H9, H11, AMANHA, sala.getId(),10,
                List.of(List.of("ESTACAO_PADRAO"), List.of("ESTACAO_EXECUTIVA")),
                CriterioProximidade.PREFERENCIAL);

        mockMvc.perform(post("/api/v1/reserva/reservaGrupo")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Tipo inexistente na sala → rejeita 409 com TIPO_ASSENTO_INDISPONIVEL")
    void tipoInexistente_rejeita() throws Exception {
        ReservaGrupoDTO dto = new ReservaGrupoDTO(H9, H11, AMANHA, sala.getId(),100,
                List.of(List.of("HOT_DESK")),
                CriterioProximidade.NENHUM);

        mockMvc.perform(post("/api/v1/reserva/reservaGrupo")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TIPO_ASSENTO_INDISPONIVEL"));
    }

    @Test
    @DisplayName("Raio configurável: raio 0.5 rejeita assentos a distância 1.0")
    void raioConfiguravelPequeno_rejeita() throws Exception {
        sala.setRaioProximidade(0.5);
        salaRepository.save(sala);

        ReservaGrupoDTO dto = new ReservaGrupoDTO(H9, H11, AMANHA, sala.getId(),10,
                List.of(List.of("ESTACAO_PADRAO"), List.of("ESTACAO_PADRAO")),
                CriterioProximidade.OBRIGATORIA);

        mockMvc.perform(post("/api/v1/reserva/reservaGrupo")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PROXIMIDADE_INSUFICIENTE"));
    }

    private Assento assento(Sala s, int posicao, String tipo, double x, double y) {
        Assento a = new Assento();
        a.setSala(s);
        a.setPosicao(posicao);
        a.setTipoAssento(tipo);
        a.setCoordenadaX(x);
        a.setCoordenadaY(y);
        a.setAtivo(true);
        return a;
    }
}