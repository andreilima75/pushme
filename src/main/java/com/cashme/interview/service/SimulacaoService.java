package com.cashme.interview.service;

import com.cashme.interview.model.Cliente;
import com.cashme.interview.model.Simulacao;
import com.cashme.interview.repository.ClienteRepository;
import com.cashme.interview.repository.SimulacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimulacaoService {

    private final SimulacaoRepository simulacaoRepository;
    private final ClienteRepository clienteRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Transactional(readOnly = true)
    public Page<Simulacao> listarPorCliente(Long clienteId, Pageable pageable) {
        validarClienteExistente(clienteId);
        return simulacaoRepository.findByClienteId(clienteId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Simulacao> listarTodas() {
        return simulacaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Simulacao buscarPorId(Long id) {
        return simulacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Simulação não encontrada com ID: " + id
                ));
    }

    @Transactional(readOnly = true)
    public List<Simulacao> buscarPorClienteId(Long clienteId) {
        validarClienteExistente(clienteId);
        return simulacaoRepository.findByClienteId(clienteId);
    }

    @Transactional
    public Simulacao criarSimulacaoEspecifica(Long clienteId) {
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

    public String gerarRelatorioTxt(List<Simulacao> simulacoes) {
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

        for (Simulacao sim : simulacoes) {
            sb.append(String.format("%-5d | %-20s | %-15.2f | %-15.2f | %-10d | %-10.2f\n",
                    sim.getId(),
                    sim.getDataHora().format(DATE_TIME_FORMATTER),
                    sim.getValorSolicitado(),
                    sim.getValorGarantia(),
                    sim.getQuantidadeMeses(),
                    sim.getTaxaJurosMensal()));
        }

        return sb.toString();
    }

    public String gerarRelatorioCsv(List<Simulacao> simulacoes) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Data,Hora,ValorSolicitado,ValorGarantia,Meses,TaxaJuros,ClienteID,ClienteNome,ClienteCPF\n");

        for (Simulacao sim : simulacoes) {
            sb.append(sim.getId()).append(",")
                    .append(sim.getDataHora().format(DATE_FORMATTER)).append(",")
                    .append(sim.getDataHora().format(TIME_FORMATTER)).append(",")
                    .append(sim.getValorSolicitado()).append(",")
                    .append(sim.getValorGarantia()).append(",")
                    .append(sim.getQuantidadeMeses()).append(",")
                    .append(sim.getTaxaJurosMensal()).append(",")
                    .append(sim.getCliente().getId()).append(",")
                    .append("\"").append(sim.getCliente().getNome()).append("\",")
                    .append(sim.getCliente().getCpf())
                    .append("\n");
        }

        return sb.toString();
    }

    private void validarClienteExistente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cliente não encontrado com ID: " + clienteId
            );
        }
    }
}