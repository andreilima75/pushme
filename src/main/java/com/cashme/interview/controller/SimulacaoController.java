package com.cashme.interview.controller;

import com.cashme.interview.model.Simulacao;
import com.cashme.interview.service.SimulacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulacoes")
@RequiredArgsConstructor
@Slf4j
public class SimulacaoController {

    private final SimulacaoService simulacaoService;

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Page<Simulacao>> listarPorCliente(
            @PathVariable Long clienteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dataHora") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Listando simulações do cliente ID: {} - página: {}, tamanho: {}", clienteId, page, size);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Simulacao> simulacoes = simulacaoService.listarPorCliente(clienteId, pageable);

        return ResponseEntity.ok(simulacoes);
    }

    @GetMapping(value = "/cliente/{clienteId}/export/txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> exportarTxt(@PathVariable Long clienteId) {
        log.info("Exportando simulações do cliente ID: {} em formato TXT", clienteId);

        List<Simulacao> simulacoes = simulacaoService.buscarPorClienteId(clienteId);

        if (simulacoes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        String relatorio = simulacaoService.gerarRelatorioTxt(simulacoes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "simulacoes_cliente_" + clienteId + ".txt");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.TEXT_PLAIN)
                .body(relatorio);
    }

    @GetMapping(value = "/cliente/{clienteId}/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportarCsv(@PathVariable Long clienteId) {
        log.info("Exportando simulações do cliente ID: {} em formato CSV", clienteId);

        List<Simulacao> simulacoes = simulacaoService.buscarPorClienteId(clienteId);

        if (simulacoes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        String relatorio = simulacaoService.gerarRelatorioCsv(simulacoes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "simulacoes_cliente_" + clienteId + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(relatorio);
    }

    @GetMapping
    public List<Simulacao> listarTodas() {
        log.info("Listando todas as simulações");
        return simulacaoService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Simulacao> buscarPorId(@PathVariable Long id) {
        log.info("Buscando simulação por ID: {}", id);
        return ResponseEntity.ok(simulacaoService.buscarPorId(id));
    }

    @PostMapping("/cliente/{clienteId}/simulacao-especifica")
    @ResponseStatus(HttpStatus.CREATED)
    public Simulacao criarSimulacaoEspecifica(@PathVariable Long clienteId) {
        log.info("Criando simulação específica para cliente ID: {}", clienteId);
        return simulacaoService.criarSimulacaoEspecifica(clienteId);
    }
}