package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.enums.CriterioProximidade;
import com.squad27.gerenciadorsalas.enums.TipoAssento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class AlocacaoService {

    private final AuditoriaService auditoriaService;

    public AlocacaoService(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    /**
     * Seleciona os melhores assentos para um grupo de pessoas.
     * Retorna a lista de assentos alocados na mesma ordem das pessoas.
     */
    public List<Assento> alocar(
            List<Assento> assentosLivres,
            List<List<TipoAssento>> tiposPreferidosPorPessoa,
            CriterioProximidade criterio,
            double raioProximidade
    ) {
        long inicio = System.currentTimeMillis();
        int numPessoas = tiposPreferidosPorPessoa.size();

        String entrada = String.format(
                "pessoas=%d | assentosLivres=%d | criterio=%s | raio=%.1f",
                numPessoas, assentosLivres.size(), criterio, raioProximidade
        );

        try {
            List<List<Assento>> candidatosPorPessoa = new ArrayList<>();
            for (int i = 0; i < numPessoas; i++) {
                List<TipoAssento> tipos = tiposPreferidosPorPessoa.get(i);
                List<Assento> candidatos = filtrarPorTipos(assentosLivres, tipos);
                if (candidatos.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Não há assentos disponíveis compatíveis com os tipos preferidos da pessoa " + (i + 1) + ".");
                }
                candidatosPorPessoa.add(candidatos);
            }

            List<Assento> melhorCombinacao = encontrarMelhorCombinacao(candidatosPorPessoa);

            if (criterio == CriterioProximidade.OBRIGATORIA) {
                double distanciaMaxima = calcularDistanciaMaxima(melhorCombinacao);
                if (distanciaMaxima > raioProximidade) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Não há posições livres e compatíveis suficientemente próximas para atender a proximidade OBRIGATÓRIA.");
                }
            }

            long duracao = System.currentTimeMillis() - inicio;
            String saida = String.format(
                    "alocado=true | assentos=%s | duracaoMs=%d",
                    melhorCombinacao.stream().map(a -> String.valueOf(a.getPosicao())).toList(),
                    duracao
            );

            auditoriaService.registrar("ALOCACAO", "ALGORITMO", null, null,
                    "ENTRADA: " + entrada + " | SAIDA: " + saida);

            return melhorCombinacao;

        } catch (ResponseStatusException ex) {
            long duracao = System.currentTimeMillis() - inicio;
            String saida = String.format(
                    "alocado=false | motivo=%s | duracaoMs=%d",
                    ex.getReason(), duracao
            );

            auditoriaService.registrar("ALOCACAO", "ALGORITMO", null, null,
                    "ENTRADA: " + entrada + " | SAIDA: " + saida);

            throw ex;
        }
    }

    /**
     * Filtra assentos por lista de tipos preferidos (ordem importa — retorna compatíveis com qualquer tipo da lista).
     * Assentos sem tipo definido são aceitos como curinga se a lista for nula/vazia.
     */
    private List<Assento> filtrarPorTipos(List<Assento> assentos, List<TipoAssento> tipos) {
        if (tipos == null || tipos.isEmpty()) {
            return new ArrayList<>(assentos);
        }
        return assentos.stream()
                .filter(a -> a.getTipoAssento() == null || tipos.contains(a.getTipoAssento()))
                .toList();
    }

    /**
     * Testa todas as combinações possíveis (uma por pessoa) e retorna
     * a que minimiza a soma das distâncias euclidianas entre os assentos.
     */
    private List<Assento> encontrarMelhorCombinacao(List<List<Assento>> candidatosPorPessoa) {
        List<Assento> melhor = null;
        double menorDistancia = Double.MAX_VALUE;

        // Iteração por força bruta — viável para salas de até 500 posições conforme RNF-03
        List<Assento> combinacaoAtual = new ArrayList<>(Collections.nCopies(candidatosPorPessoa.size(), null));
        melhor = buscarCombinacao(candidatosPorPessoa, combinacaoAtual, 0, melhor, new double[]{menorDistancia});

        return melhor;
    }

    private List<Assento> buscarCombinacao(
            List<List<Assento>> candidatos,
            List<Assento> atual,
            int pessoa,
            List<Assento> melhor,
            double[] menorDistancia
    ) {
        if (pessoa == candidatos.size()) {
            // Verifica se não há assento repetido na combinação
            Set<Integer> ids = new HashSet<>();
            for (Assento a : atual) ids.add(a.getId());
            if (ids.size() < atual.size()) return melhor; // combinação inválida — assento duplicado

            double dist = calcularDistanciaTotal(atual);
            if (dist < menorDistancia[0]) {
                menorDistancia[0] = dist;
                return new ArrayList<>(atual);
            }
            return melhor;
        }

        for (Assento candidato : candidatos.get(pessoa)) {
            atual.set(pessoa, candidato);
            melhor = buscarCombinacao(candidatos, atual, pessoa + 1, melhor, menorDistancia);
        }
        return melhor;
    }

    /**
     * Soma das distâncias euclidianas entre todos os pares de assentos.
     * Assentos sem coordenada recebem distância 0 (não penalizam nem beneficiam).
     */
    private double calcularDistanciaTotal(List<Assento> assentos) {
        double total = 0;
        for (int i = 0; i < assentos.size(); i++) {
            for (int j = i + 1; j < assentos.size(); j++) {
                total += distanciaEuclidiana(assentos.get(i), assentos.get(j));
            }
        }
        return total;
    }

    /**
     * Maior distância entre qualquer par — usada para validar proximidade obrigatória.
     */
    private double calcularDistanciaMaxima(List<Assento> assentos) {
        double maxima = 0;
        for (int i = 0; i < assentos.size(); i++) {
            for (int j = i + 1; j < assentos.size(); j++) {
                maxima = Math.max(maxima, distanciaEuclidiana(assentos.get(i), assentos.get(j)));
            }
        }
        return maxima;
    }

    private double distanciaEuclidiana(Assento a, Assento b) {
        if (a.getCoordenadaX() == null || a.getCoordenadaY() == null
                || b.getCoordenadaX() == null || b.getCoordenadaY() == null) {
            return 0;
        }
        double dx = a.getCoordenadaX() - b.getCoordenadaX();
        double dy = a.getCoordenadaY() - b.getCoordenadaY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}