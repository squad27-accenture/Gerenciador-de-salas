package com.squad27.gerenciadorsalas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.squad27.gerenciadorsalas.domain.*;
import com.squad27.gerenciadorsalas.dto.ReservaDTO;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservaIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ReservaRepository reservaRepository;
    @Autowired SalaRepository salaRepository;
    @Autowired AssentoRepository assentoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired TokenService tokenService;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired DisponibilidadeSalaRepository disponibilidadeSalaRepository;
    @MockitoBean NotificacaoEmailService notificacaoEmailService;

    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    Sala sala;
    Usuario usuario;
    Usuario outroUsuario;
    String tokenUsuario;
    String tokenOutroUsuario;

    static final LocalDate AMANHA = LocalDate.now().plusDays(1);
    static final LocalTime H9  = LocalTime.of(9, 0);
    static final LocalTime H10 = LocalTime.of(10, 0);
    static final LocalTime H11 = LocalTime.of(11, 0);

    @BeforeEach
    void setup() {
        sala = new Sala();
        sala.setNome("Sala Teste");
        sala.setCapacidade(10);
        sala.setDeletado(false);
        sala.setStatus(StatusSala.DISPONIVEL);
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

        Assento a1 = new Assento(); a1.setSala(sala); a1.setPosicao(1); a1.setAtivo(true);
        Assento a2 = new Assento(); a2.setSala(sala); a2.setPosicao(2); a2.setAtivo(true);
        assentoRepository.saveAll(List.of(a1, a2));

        usuario = new Usuario("user@teste.com", passwordEncoder.encode("senha"), Role.USER, "User Teste", TipoFuncionario.OUTRO);
        usuario = usuarioRepository.save(usuario);

        outroUsuario = new Usuario("outro@teste.com", passwordEncoder.encode("senha"), Role.USER, "Outro", TipoFuncionario.OUTRO);
        outroUsuario = usuarioRepository.save(outroUsuario);

        tokenUsuario      = "Bearer " + tokenService.generateToken(usuario);
        tokenOutroUsuario = "Bearer " + tokenService.generateToken(outroUsuario);
    }

    // ── RESERVA INDIVIDUAL ────────────────────────────────────────────

    @Test
    @DisplayName("Reserva individual: sucesso")
    void reservaIndividual_sucesso() throws Exception {
        ReservaDTO dto = dtoPessoa(H9, H10, AMANHA, sala.getId());

        mockMvc.perform(post("/api/v1/reserva/realizarReserva")
                        .header("Authorization", tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusReserva").value("CONFIRMADA"))
                .andExpect(jsonPath("$.salaId").value(sala.getId()));
    }

    @Test
    @DisplayName("Reserva individual: conflito exato de horário → 409")
    void reservaIndividual_conflitoExato() throws Exception {
        reservaExistente(1, AMANHA, H9, H10);
        reservaExistente(2, AMANHA, H9, H10);

        ReservaDTO dto = dtoPessoa(H9, H10, AMANHA, sala.getId());

        mockMvc.perform(post("/api/v1/reserva/realizarReserva")
                        .header("Authorization", tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reserva individual: sobreposição parcial de horário → 409")
    void reservaIndividual_conflitoSobreposicao() throws Exception {
        reservaExistente(1, AMANHA, H9, H11);
        reservaExistente(2, AMANHA, H9, H11);

        ReservaDTO dto = dtoPessoa(H10, H11, AMANHA, sala.getId());

        mockMvc.perform(post("/api/v1/reserva/realizarReserva")
                        .header("Authorization", tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reserva individual: assentos diferentes no mesmo horário não conflitam")
    void reservaIndividual_assentosDiferentesNaoConflitam() throws Exception {
        ReservaDTO dto1 = dtoPessoa(H9, H10, AMANHA, sala.getId());
        mockMvc.perform(post("/api/v1/reserva/realizarReserva")
                        .header("Authorization", tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto1)))
                .andExpect(status().isOk());

        ReservaDTO dto2 = dtoPessoa(H9, H10, AMANHA, sala.getId());
        mockMvc.perform(post("/api/v1/reserva/realizarReserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto2))
                        .header("Authorization", tokenUsuario))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Reserva individual: slot de reserva cancelada fica disponível")
    void reservaIndividual_canceladaLiberaSlot() throws Exception {
        Reserva cancelada = reservaExistente(1, AMANHA, H9, H10);
        reservaExistente(2, AMANHA, H9, H10);

        cancelada.setStatusReserva(StatusReserva.CANCELADA);
        reservaRepository.save(cancelada);

        ReservaDTO dto = dtoPessoa(H9, H10, AMANHA, sala.getId());
        mockMvc.perform(post("/api/v1/reserva/realizarReserva")
                        .header("Authorization", tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Reserva individual: horário início >= fim → 400")
    void reservaIndividual_horarioInvalido() throws Exception {
        ReservaDTO dto = dtoPessoa(H10, H9, AMANHA, sala.getId());

        mockMvc.perform(post("/api/v1/reserva/realizarReserva")
                        .header("Authorization", tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Reserva individual: sem token → 401")
    void reservaIndividual_semAutenticacao() throws Exception {
        ReservaDTO dto = dtoPessoa(H9, H10, AMANHA, sala.getId());

        mockMvc.perform(post("/api/v1/reserva/realizarReserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ── RESERVA EM GRUPO ─────────────────────────────────────────────

    @Test
    @DisplayName("Reserva em grupo: sucesso — retorna lista com mesmo codigoGrupo")
    void reservaGrupo_sucesso() throws Exception {
        ReservaGrupoDTO dto = new ReservaGrupoDTO(H9, H10, AMANHA, sala.getId(),10,
                List.of(List.of(), List.of()), CriterioProximidade.NENHUM);

        String responseJson = mockMvc.perform(post("/api/v1/reserva/reservaGrupo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto))
                        .header("Authorization", tokenUsuario))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].codigoGrupo").isNotEmpty())
                .andExpect(jsonPath("$[1].codigoGrupo").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String codigo0 = mapper.readTree(responseJson).get(0).get("codigoGrupo").asText();
        String codigo1 = mapper.readTree(responseJson).get(1).get("codigoGrupo").asText();
        assert codigo0.equals(codigo1) : "codigoGrupo deve ser igual nos dois assentos";
    }

    @Test
    @DisplayName("Reserva em grupo: conflito em qualquer assento → 409")
    void reservaGrupo_conflito() throws Exception {
        reservaExistente(1, AMANHA, H9, H10);
        reservaExistente(2, AMANHA, H9, H10);

        ReservaGrupoDTO dto = new ReservaGrupoDTO(H9, H10, AMANHA, sala.getId(), 10,
                List.of(List.of(), List.of()), CriterioProximidade.NENHUM);

        mockMvc.perform(post("/api/v1/reserva/reservaGrupo")
                        .header("Authorization", tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // ── CANCELAMENTO INDIVIDUAL ───────────────────────────────────────

    @Test
    @DisplayName("Cancelar reserva: sucesso → statusReserva = CANCELADA")
    void cancelarReserva_sucesso() throws Exception {
        Reserva reserva = reservaExistente(1, AMANHA, H9, H10);

        mockMvc.perform(put("/api/v1/reserva/{id}/cancelar", reserva.getId())
                        .header("Authorization", tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusReserva").value("CANCELADA"));
    }

    @Test
    @DisplayName("Cancelar reserva: usuário diferente do dono → erro")
    void cancelarReserva_usuarioErrado() throws Exception {
        Reserva reserva = reservaExistente(1, AMANHA, H9, H10);

        mockMvc.perform(put("/api/v1/reserva/{id}/cancelar", reserva.getId())
                        .header("Authorization", tokenOutroUsuario))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Cancelar reserva: id inexistente → erro")
    void cancelarReserva_inexistente() throws Exception {
        mockMvc.perform(put("/api/v1/reserva/{id}/cancelar", 9999)
                        .header("Authorization", tokenUsuario))
                .andExpect(status().is4xxClientError());
    }

    // ── CANCELAMENTO EM GRUPO ─────────────────────────────────────────

    @Test
    @DisplayName("Cancelar em grupo: sucesso → todas CANCELADAS")
    void cancelarReservaGrupo_sucesso() throws Exception {
        String codigo = "grupo-teste-abc";
        reservaComGrupo(1, AMANHA, H9, H10, codigo, usuario);
        reservaComGrupo(2, AMANHA, H9, H10, codigo, usuario);

        mockMvc.perform(put("/api/v1/reserva/grupo/{codigo}/cancelar", codigo)
                        .header("Authorization", tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].statusReserva").value("CANCELADA"))
                .andExpect(jsonPath("$[1].statusReserva").value("CANCELADA"));
    }

    @Test
    @DisplayName("Cancelar em grupo: código inexistente → erro")
    void cancelarReservaGrupo_codigoInexistente() throws Exception {
        mockMvc.perform(put("/api/v1/reserva/grupo/{codigo}/cancelar", "nao-existe")
                        .header("Authorization", tokenUsuario))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Cancelar em grupo: usuário diferente do dono → erro")
    void cancelarReservaGrupo_usuarioErrado() throws Exception {
        String codigo = "grupo-alheio";
        reservaComGrupo(1, AMANHA, H9, H10, codigo, usuario);

        mockMvc.perform(put("/api/v1/reserva/grupo/{codigo}/cancelar", codigo)
                        .header("Authorization", tokenOutroUsuario))
                .andExpect(status().is4xxClientError());
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    private ReservaDTO dtoPessoa(LocalTime inicio, LocalTime fim, LocalDate data, Integer salaId) {
        return new ReservaDTO(inicio, fim, data, salaId, List.of(), null, null, 1, CriterioProximidade.NENHUM);
    }

    private Reserva reservaExistente(int posicao, LocalDate data, LocalTime inicio, LocalTime fim) {
        Reserva r = new Reserva();
        r.setSala(sala); r.setUsuario(usuario);
        r.setPosicaoAssento(posicao); r.setDataReserva(data);
        r.setHorarioInicio(inicio); r.setHorarioFim(fim);
        r.setStatusReserva(StatusReserva.EmANDAMENTO);
        return reservaRepository.save(r);
    }

    private Reserva reservaComGrupo(int posicao, LocalDate data, LocalTime inicio, LocalTime fim,
                                    String codigoGrupo, Usuario dono) {
        Reserva r = new Reserva();
        r.setSala(sala); r.setUsuario(dono);
        r.setPosicaoAssento(posicao); r.setDataReserva(data);
        r.setHorarioInicio(inicio); r.setHorarioFim(fim);
        r.setStatusReserva(StatusReserva.EmANDAMENTO);
        r.setCodigoGrupo(codigoGrupo);
        return reservaRepository.save(r);
    }
}