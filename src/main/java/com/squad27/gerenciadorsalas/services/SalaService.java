package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.dto.AssentoReponseDTO;
import com.squad27.gerenciadorsalas.dto.SalaDTO;
import com.squad27.gerenciadorsalas.dto.SalaResponseDTO;
import com.squad27.gerenciadorsalas.repositories.AssentoRepository;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
                        assento.getTipoAssento() == null ? null : assento.getTipoAssento().name(),
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
}