package com.squad27.gerenciadorsalas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.squad27.gerenciadorsalas.domain.*;
import com.squad27.gerenciadorsalas.dto.ReservaDTO;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DisponibilidadeIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired SalaRepository salaRepository;
    @Autowired AssentoRepository assentoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired DisponibilidadeSalaRepository disponibilidadeSalaRepository;
    @Autowired DataBloqueadaRepository dataBloqueadaRepository;
    @Autowired ReservaRepository reservaRepository;
    @Autowired TokenService tokenService;
    @Autowired PasswordEncoder passwordEncoder;
    @MockitoBean NotificacaoEmailService notificacaoEmailService;

    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    Sala sala;
    Usuario usuario;
    String token;

    static final LocalTime H7  = LocalTime.of(7, 0);
    static final LocalTime H9  = LocalTime.of(9, 0);
    static final LocalTime H10 = LocalTime.of(10, 0);
    static final LocalTime H22 = LocalTime.of(22, 0);

    // usa uma data futura fixa para evitar dependência do dia atual
    static final LocalDate DATA_FUTURA = LocalDate.now().plusDays(10);

    @BeforeEach
    void setup() {
        sala = new Sala();
        sala.setNome("Sala Disp Test");
        sala.setCapacidade(2);
        sala.setDeletado(false);
        sala.setStatus(StatusSala.DISPONIVEL);
        sala.setRaioProximidade(5.0);
        sala = salaRepository.save(sala);

        Assento a1 = new Assento(); a1.setSala(sala); a1.setPosicao(1); a1.setAtivo(true);
        Assento a2 = new Assento(); a2.setSala(sala); a2.setPosicao(2); a2.setAtivo(true);
        assentoRepository.saveAll(List.of(a1, a2));

        usuario = new Usuario("u@disp.com", passwordEncoder.encode("123"), Role.USER, "Disp Tester", TipoFuncionario.OUTRO);
        usuario = usuarioRepository.save(usuario);
        token = "Bearer " + tokenService.generateToken(usuario);
    }

    // ── SEM CONFIGURAÇÃO ─────────────────────────────────────────────

    @Test
    @DisplayName("Reservar em sala sem configuração de disponibilidade → 422")
    void semConfiguracao_rejeita() throws Exception {
        ReservaDTO dto = dtoReserva(H9, H10, DATA_FUTURA, sala.getId());

        mockMvc.perform(post("/api/v1/reserva/realizarReserva")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());
    }

    // ── DATA BLOQUEADA ───────────────────────────────────────────────

    @Test
    @DisplayName("Reservar em data bloqueada → 422")
    void dataBloqueada_rejeita() throws Exception {
        configurarDisponibilidade(DiaSemana.values());

        DataBloqueada bloqueio = new DataBloqueada();
        bloqueio.setSala(sala);
        bloqueio.setData(DATA_FUTURA);
        bloqueio.setMotivo("Feriado interno");
        dataBloqueadaRepository.save(bloqueio);

        ReservaDTO dto = dtoReserva(H9, H10, DATA_FUTURA, sala.getId());

        mockMvc.perform(post("/api/v1/reserva/realizarReserva")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());
    }

    // ── HORÁRIO FORA DO FUNCIONAMENTO ────────────────────────────────

    @Test
    @DisplayName("Reservar fora do horário de funcionamento → 422")
    void foraDoHorario_rejeita() throws Exception {
        configurarDisponibilidade(DiaSemana.values());

        // horário de fechamento é 22h, tenta reservar das 21h às 23h
        ReservaDTO dto = dtoReserva(LocalTime.of(21, 0), LocalTime.of(23, 0), DATA_FUTURA, sala.getId());

        mockMvc.perform(post("/api/v1/reserva/realizarReserva")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());
    }

    // ── CONSULTA POR PERÍODO ─────────────────────────────────────────

    @Test
    @DisplayName("Disponibilidade por período: dias configurados retornam DISPONIVEL")
    void periodo_retornaDisponivel() throws Exception {
        configurarDisponibilidade(DiaSemana.values());
        LocalDate inicio = DATA_FUTURA;
        LocalDate fim    = DATA_FUTURA.plusDays(2);

        mockMvc.perform(get("/api/v1/salas/{id}/disponibilidade/periodo", sala.getId())
                        .param("dataInicio", inicio.toString())
                        .param("dataFim", fim.toString())
                        .param("horarioInicio", "09:00:00")
                        .param("horarioFim", "10:00:00")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].status").value("DISPONIVEL"))
                .andExpect(jsonPath("$[0].totalAssentos").value(2))
                .andExpect(jsonPath("$[0].assentosLivres").value(2));
    }

    @Test
    @DisplayName("Disponibilidade por período: data bloqueada aparece como BLOQUEADA")
    void periodo_dataBloqueadaApareceBloqueada() throws Exception {
        configurarDisponibilidade(DiaSemana.values());

        DataBloqueada bloqueio = new DataBloqueada();
        bloqueio.setSala(sala);
        bloqueio.setData(DATA_FUTURA);
        bloqueio.setMotivo("Manutenção");
        dataBloqueadaRepository.save(bloqueio);

        mockMvc.perform(get("/api/v1/salas/{id}/disponibilidade/periodo", sala.getId())
                        .param("dataInicio", DATA_FUTURA.toString())
                        .param("dataFim", DATA_FUTURA.toString())
                        .param("horarioInicio", "09:00:00")
                        .param("horarioFim", "10:00:00")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("BLOQUEADA"))
                .andExpect(jsonPath("$[0].motivo").value("Manutenção"))
                .andExpect(jsonPath("$[0].assentosOcupados").value(0))
                .andExpect(jsonPath("$[0].assentosLivres").value(0));
    }

    @Test
    @DisplayName("Disponibilidade por período: reserva ativa reduz assentosLivres")
    void periodo_reservaAtivaReducLivres() throws Exception {
        configurarDisponibilidade(DiaSemana.values());

        Reserva r = new Reserva();
        r.setSala(sala); r.setUsuario(usuario);
        r.setPosicaoAssento(1); r.setDataReserva(DATA_FUTURA);
        r.setHorarioInicio(H9); r.setHorarioFim(H10);
        r.setStatusReserva(StatusReserva.EmANDAMENTO);
        reservaRepository.save(r);

        mockMvc.perform(get("/api/v1/salas/{id}/disponibilidade/periodo", sala.getId())
                        .param("dataInicio", DATA_FUTURA.toString())
                        .param("dataFim", DATA_FUTURA.toString())
                        .param("horarioInicio", "09:00:00")
                        .param("horarioFim", "10:00:00")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assentosOcupados").value(1))
                .andExpect(jsonPath("$[0].assentosLivres").value(1));
    }

    @Test
    @DisplayName("Disponibilidade por período: período maior que 3 meses → 400")
    void periodo_acimaDe3Meses_rejeita() throws Exception {
        mockMvc.perform(get("/api/v1/salas/{id}/disponibilidade/periodo", sala.getId())
                        .param("dataInicio", LocalDate.now().toString())
                        .param("dataFim", LocalDate.now().plusMonths(4).toString())
                        .param("horarioInicio", "09:00:00")
                        .param("horarioFim", "10:00:00")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    private void configurarDisponibilidade(DiaSemana... dias) {
        for (DiaSemana dia : dias) {
            DisponibilidadeSala disp = new DisponibilidadeSala();
            disp.setSala(sala);
            disp.setDiaSemana(dia);
            disp.setAceitaReservas(true);
            disp.setHorarioAbertura(H7);
            disp.setHorarioFechamento(H22);
            disp.setAntecedenciaMinimaDias(0);
            disponibilidadeSalaRepository.save(disp);
        }
    }

    private ReservaDTO dtoReserva(LocalTime inicio, LocalTime fim, LocalDate data, Integer salaId) {
        return new ReservaDTO(inicio, fim, data, salaId, List.of(), null, null, 1, CriterioProximidade.NENHUM);
    }
}