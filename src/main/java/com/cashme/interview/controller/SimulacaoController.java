package com.cashme.interview.controller;

import com.cashme.interview.model.Simulacao;
import com.cashme.interview.model.Cliente;
import com.cashme.interview.repository.SimulacaoRepository;
import com.cashme.interview.repository.ClienteRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/simulacoes")
@RequiredArgsConstructor
@Slf4j
public class SimulacaoController {

    private final SimulacaoRepository simulacaoRepository;
    private final ClienteRepository clienteRepository;

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Page<Simulacao>> listarPorCliente(
            @PathVariable Long clienteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dataHora") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Listando simulações do cliente ID: {} - página: {}, tamanho: {}", clienteId, page, size);

        if (!clienteRepository.existsById(clienteId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cliente não encontrado com ID: " + clienteId
            );
        }

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Simulacao> simulacoes = simulacaoRepository.findByClienteId(clienteId, pageable);

        return ResponseEntity.ok(simulacoes);
    }

    @GetMapping(value = "/cliente/{clienteId}/export/txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> exportarTxt(@PathVariable Long clienteId) {
        log.info("Exportando simulações do cliente ID: {} em formato TXT", clienteId);

        if (!clienteRepository.existsById(clienteId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cliente não encontrado com ID: " + clienteId
            );
        }

        List<Simulacao> simulacoes = simulacaoRepository.findByClienteId(clienteId);

        if (simulacoes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Cliente cliente = simulacoes.getFirst().getCliente();

        StringBuilder sb = new StringBuilder();
        sb.append("RELATÓRIO DE SIMULAÇÕES\n");
        sb.append("========================\n\n");
        sb.append("Cliente: ").append(cliente.getNome()).append("\n");
        sb.append("CPF: ").append(cliente.getCpf()).append("\n");
        sb.append("Total de simulações: ").append(simulacoes.size()).append("\n\n");

        sb.append(String.format("%-5s | %-20s | %-15s | %-15s | %-10s | %-10s\n",
                "ID", "Data/Hora", "Valor Solicitado", "Valor Garantia", "Meses", "Taxa %"));
        sb.append("----------------------------------------------------------------------------------------\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (Simulacao sim : simulacoes) {
            sb.append(String.format("%-5d | %-20s | %-15.2f | %-15.2f | %-10d | %-10.2f\n",
                    sim.getId(),
                    sim.getDataHora().format(formatter),
                    sim.getValorSolicitado(),
                    sim.getValorGarantia(),
                    sim.getQuantidadeMeses(),
                    sim.getTaxaJurosMensal()));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "simulacoes_cliente_" + clienteId + ".txt");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.TEXT_PLAIN)
                .body(sb.toString());
    }

    @GetMapping(value = "/cliente/{clienteId}/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportarCsv(@PathVariable Long clienteId) {
        log.info("Exportando simulações do cliente ID: {} em formato CSV", clienteId);

        if (!clienteRepository.existsById(clienteId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cliente não encontrado com ID: " + clienteId
            );
        }

        List<Simulacao> simulacoes = simulacaoRepository.findByClienteId(clienteId);

        if (simulacoes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Data,Hora,ValorSolicitado,ValorGarantia,Meses,TaxaJuros,ClienteID,ClienteNome,ClienteCPF\n");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (Simulacao sim : simulacoes) {
            sb.append(sim.getId()).append(",")
                    .append(sim.getDataHora().format(dateFormatter)).append(",")
                    .append(sim.getDataHora().format(timeFormatter)).append(",")
                    .append(sim.getValorSolicitado()).append(",")
                    .append(sim.getValorGarantia()).append(",")
                    .append(sim.getQuantidadeMeses()).append(",")
                    .append(sim.getTaxaJurosMensal()).append(",")
                    .append(sim.getCliente().getId()).append(",")
                    .append("\"").append(sim.getCliente().getNome()).append("\",")
                    .append(sim.getCliente().getCpf())
                    .append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "simulacoes_cliente_" + clienteId + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(sb.toString());
    }


    @GetMapping
    public List<Simulacao> listarTodas() {
        log.info("Listando todas as simulações");
        return simulacaoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Simulacao> buscarPorId(@PathVariable Long id) {
        log.info("Buscando simulação por ID: {}", id);
        return simulacaoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Simulação não encontrada com ID: " + id
                ));
    }

    @PostMapping("/cliente/{clienteId}/simulacao-especifica")
    @ResponseStatus(HttpStatus.CREATED)
    public Simulacao criarSimulacaoEspecifica(@PathVariable Long clienteId) {
        log.info("Criando simulação específica para cliente ID: {}", clienteId);

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente não encontrado com ID: " + clienteId
                ));

        Simulacao simulacao = new Simulacao();
        simulacao.setCliente(cliente);
        simulacao.setDataHora(LocalDateTime.of(2024, 6, 15, 10, 30, 26));
        simulacao.setValorSolicitado(new BigDecimal("300000.00"));
        simulacao.setValorGarantia(new BigDecimal("1000000.00"));
        simulacao.setQuantidadeMeses(150);
        simulacao.setTaxaJurosMensal(new BigDecimal("2.00"));

        return simulacaoRepository.save(simulacao);
    }
}