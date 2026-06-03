package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.dto.*;
import com.squad27.gerenciadorsalas.enums.StatusLayout;
import com.squad27.gerenciadorsalas.repositories.AssentoRepository;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class SalaService {

    private final SalaRepository repository;
    private final AssentoRepository assentoRepository;
    private final AuditoriaService auditoriaService;

    public SalaService(SalaRepository repository, AssentoRepository assentoRepository,
                       AuditoriaService auditoriaService) {
        this.repository = repository;
        this.assentoRepository = assentoRepository;
        this.auditoriaService = auditoriaService;
    }

    public Sala cadastrarsala(SalaDTO salaDTO, String emailUsuario){
        Sala sala = new Sala();
        String nome = salaDTO.nome().trim();

        if (repository.existsByNomeIgnoreCaseAndDeletadoFalse(nome)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma sala com esse nome."
            );
        }

        if (salaDTO.nome()  ==  null || salaDTO.nome().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O nome da sala é obrigatorio.");
        }
        if (salaDTO.capacidade() <=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A capacidade da sala deve ser maior que 0.");
        }

        if (salaDTO.local() == null || salaDTO.local().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,
                    "O local é obrigatorio");
        }
        if (salaDTO.statusSala() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O status da sala é obrigatorio");
        }
        if (salaDTO.capacidade() > 0){
            for(int c = 1; c <= salaDTO.capacidade(); c++){
                Assento assento = new Assento();
                assento.setPosicao(c);

                sala.adicionarasseto(assento);
            }
        }
        sala.setNome(nome);
        sala.setCapacidade(salaDTO.capacidade());
        sala.setStatus(salaDTO.statusSala());
        sala.setLocal(salaDTO.local());
        sala.setCidade(salaDTO.cidade());
        sala.setEstado(salaDTO.estado());
        sala.setAndar(salaDTO.andar());
        sala.setBloco(salaDTO.bloco());
        sala.setEquipamentosSala(salaDTO.equipamentosSala());
        sala.setDeletado(false);
        sala.setRaioProximidade(salaDTO.raioProximidade() != null ? salaDTO.raioProximidade() : 5.0);
        Sala salva = repository.save(sala);
        auditoriaService.registrar(
                "CRIACAO", "SALA", String.valueOf(salva.getId()),
                emailUsuario,
                "Sala criada: " + salva.getNome() + " — capacidade " + salva.getCapacidade()
        );
        return salva;
    }

    public List<SalaResponseDTO> listarSalas() {
        return repository.findAllByDeletadoFalse()
                .stream()
                .map(sala -> new SalaResponseDTO(
                        sala.getId(),
                        sala.getNome(),
                        sala.getCapacidade(),
                        sala.getLocal(),
                        sala.getCidade(),
                        sala.getEstado(),
                        sala.getAndar(),
                        sala.getBloco()
                ))
                .toList();
    }

    @Transactional
    public void deletarSalaPorId(Integer id, String emailUsuario) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada.");
        }
        repository.softDeleteById(id);
        auditoriaService.registrar(
                "EXCLUSAO", "SALA", String.valueOf(id),
                emailUsuario,
                "Sala removida (soft delete)"
        );
    }

    @Transactional
    public void deletarSalaPorNome(String nome) {
        repository.softDeleteByNome(nome);
    }

    @Transactional
    public void atualizarSalaPorId(Integer id, Sala sala, String emailUsuario) {

        Sala salaEntity = repository.findByIdAndDeletadoFalse(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada.")
        );

        if (sala.getNome() != null)       salaEntity.setNome(sala.getNome());
        if (sala.getStatus() != null)     salaEntity.setStatus(sala.getStatus());
        if (sala.getLocal() != null)      salaEntity.setLocal(sala.getLocal());
        if (sala.getCidade() != null)     salaEntity.setCidade(sala.getCidade());
        if (sala.getEstado() != null)     salaEntity.setEstado(sala.getEstado());

        if (sala.getCapacidade() != null && !sala.getCapacidade().equals(salaEntity.getCapacidade())) {
            int capacidadeAtual = assentoRepository.findBySalaIdOrderByPosicao(id).size();
            int novaCapacidade = sala.getCapacidade();

            if (novaCapacidade <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "A capacidade da sala deve ser maior que 0.");
            }

            if (novaCapacidade > capacidadeAtual) {
                // adiciona apenas os assentos que faltam, preservando os existentes
                for (int c = capacidadeAtual + 1; c <= novaCapacidade; c++) {
                    Assento assento = new Assento();
                    assento.setPosicao(c);
                    salaEntity.adicionarasseto(assento);
                }
            } else if (novaCapacidade < capacidadeAtual) {
                // inativa os assentos excedentes em vez de deletar (preserva histórico)
                List<Assento> todos = assentoRepository.findBySalaIdOrderByPosicao(id);
                for (int i = novaCapacidade; i < capacidadeAtual; i++) {
                    todos.get(i).setAtivo(false);
                }
                assentoRepository.saveAll(todos.subList(novaCapacidade, capacidadeAtual));
            }

            salaEntity.setCapacidade(novaCapacidade);
        }

        repository.save(salaEntity);
        auditoriaService.registrar(
                "EDICAO", "SALA", String.valueOf(id),
                emailUsuario,
                "Sala atualizada: " + salaEntity.getNome()
        );
    }

    public List<AssentoReponseDTO> listarAssentosDaSala(Integer salaId) {
        return assentoRepository.findBySalaIdOrderByPosicao(salaId)
                .stream()
                .map(assento -> new AssentoReponseDTO(
                        assento.getId(),
                        assento.getPosicao(),
                        assento.getTipoAssento(),
                        assento.getCoordenadaX(),
                        assento.getCoordenadaY(),
                        assento.getTipoCadeira(),
                        assento.getTipoMesa(),
                        assento.getAtivo(),
                        assento.getEquipamentos()
                                .stream()
                                .map(Enum::name)
                                .toList()
                ))
                .toList();
    }

    public SalaResponseDTO uploadLayout(Integer salaId, MultipartFile imagem, String emailUsuario) {
        Sala sala = repository.findByIdAndDeletadoFalse(salaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        if (sala.getStatusLayout() == StatusLayout.AGUARDANDO_LAYOUT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sala já possui um layout em processamento.");
        }

        try {
            String nomeArquivo = "sala_" + salaId + "_" + System.currentTimeMillis() + "_" + imagem.getOriginalFilename();
            Path destino = Paths.get("uploads/layouts/" + nomeArquivo);
            Files.createDirectories(destino.getParent());
            Files.copy(imagem.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            sala.setImagemUrl(destino.toString());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar imagem.");
        }

        sala.setStatusLayout(StatusLayout.AGUARDANDO_LAYOUT);
        Sala salva = repository.save(sala);

        auditoriaService.registrar("UPLOAD_LAYOUT", "SALA", String.valueOf(salaId),
                emailUsuario, "Imagem de layout enviada para processamento.");

        return toResponseDTO(salva);
    }

    public LayoutPreviewDTO layoutPreview(Integer salaId) {
        Sala sala = repository.findByIdAndDeletadoFalse(salaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        List<AssentoReponseDTO> assentos = assentoRepository.findBySalaIdOrderByPosicao(salaId)
                .stream()
                .map(a -> new AssentoReponseDTO(
                        a.getId(),
                        a.getPosicao(),
                        a.getTipoAssento(),
                        a.getCoordenadaX(),
                        a.getCoordenadaY(),
                        a.getTipoCadeira(),
                        a.getTipoMesa(),
                        a.getAtivo(),
                        a.getEquipamentos().stream().map(Enum::name).toList()
                ))
                .toList();

        return new LayoutPreviewDTO(
                sala.getId(),
                sala.getNome(),
                sala.getStatusLayout(),
                sala.getImagemUrl(),
                assentos
        );
    }

    public SalaResponseDTO aprovarLayout(Integer salaId, AprovarLayoutDTO dto, String emailUsuario) {
        Sala sala = repository.findByIdAndDeletadoFalse(salaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        if (sala.getStatusLayout() != StatusLayout.LAYOUT_PENDENTE_VALIDACAO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O layout da sala não está pendente de validação.");
        }

        if (!dto.aprovado()) {
            sala.setStatusLayout(StatusLayout.SEM_LAYOUT);
            repository.save(sala);
            auditoriaService.registrar("REJEICAO_LAYOUT", "SALA", String.valueOf(salaId),
                    emailUsuario, "Layout rejeitado pelo administrador.");
            return toResponseDTO(sala);
        }

        // Aplica ajustes manuais se houver (RF-05)
        if (dto.assentos() != null && !dto.assentos().isEmpty()) {
            for (AssentoLayoutDTO ajuste : dto.assentos()) {
                assentoRepository.findBySalaIdAndPosicao(salaId, ajuste.posicao())
                        .ifPresent(assento -> {
                            if (ajuste.coordenadaX() != null) assento.setCoordenadaX(ajuste.coordenadaX());
                            if (ajuste.coordenadaY() != null) assento.setCoordenadaY(ajuste.coordenadaY());
                            if (ajuste.tipoAssento() != null) assento.setTipoAssento(ajuste.tipoAssento().trim().toUpperCase());
                            if (ajuste.equipamentos() != null) assento.setEquipamentos(ajuste.equipamentos());
                            assentoRepository.save(assento);
                        });
            }
        }

        sala.setStatusLayout(StatusLayout.ATIVA);
        Sala salva = repository.save(sala);

        auditoriaService.registrar("APROVACAO_LAYOUT", "SALA", String.valueOf(salaId),
                emailUsuario, "Layout aprovado pelo administrador.");

        return toResponseDTO(salva);
    }

    private SalaResponseDTO toResponseDTO(Sala sala) {
        return new SalaResponseDTO(
                sala.getId(),
                sala.getNome(),
                sala.getCapacidade(),
                sala.getLocal(),
                sala.getCidade(),
                sala.getEstado(),
                sala.getAndar(),
                sala.getBloco()
        );
    }

    public void receberResultadoAgente(AgentLayoutResultDTO dto) {
        Sala sala = repository.findByIdAndDeletadoFalse(dto.salaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        if (sala.getStatusLayout() != StatusLayout.AGUARDANDO_LAYOUT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A sala não está aguardando processamento de layout.");
        }

        // Remove assentos gerados anteriormente se houver
        List<Assento> assentosExistentes = assentoRepository.findBySalaIdOrderByPosicao(sala.getId());
        assentoRepository.deleteAll(assentosExistentes);

        // Cria os assentos com base no resultado do agente
        for (AgentPosicaoDTO posicao : dto.posicoes()) {
            Assento assento = new Assento();
            assento.setSala(sala);
            assento.setPosicao(posicao.posicao());
            assento.setCoordenadaX(posicao.coordenadaX());
            assento.setCoordenadaY(posicao.coordenadaY());
            assento.setTipoAssento(posicao.tipoAssento() != null ? posicao.tipoAssento().trim().toUpperCase() : null);
            assento.setEquipamentos(posicao.equipamentos() != null ? posicao.equipamentos() : List.of());
            assento.setAtivo(true);
            assentoRepository.save(assento);
        }

        sala.setStatusLayout(StatusLayout.LAYOUT_PENDENTE_VALIDACAO);
        sala.setCapacidade(dto.posicoes().size());
        repository.save(sala);

        auditoriaService.registrar("RESULTADO_AGENTE", "SALA", String.valueOf(sala.getId()),
                null, "Agente processou layout e gerou " + dto.posicoes().size() + " posições.");
    }


}