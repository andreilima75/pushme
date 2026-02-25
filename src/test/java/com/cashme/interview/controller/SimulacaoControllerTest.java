package com.cashme.interview.controller;

import com.cashme.interview.model.Cliente;
import com.cashme.interview.model.Endereco;
import com.cashme.interview.model.Simulacao;
import com.cashme.interview.repository.ClienteRepository;
import com.cashme.interview.repository.SimulacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SimulacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private SimulacaoRepository simulacaoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Cliente clienteTeste;

    @BeforeEach
    void setup() {
        simulacaoRepository.deleteAll();
        clienteRepository.deleteAll();

        Endereco endereco = new Endereco();
        endereco.setRua("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Centro");
        endereco.setCep("12345-678");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");

        clienteTeste = new Cliente();
        clienteTeste.setCpf("12345678901");
        clienteTeste.setNome("João Teste");
        clienteTeste.setEndereco(endereco);

        clienteTeste = clienteRepository.save(clienteTeste);
    }

    @Test
    void testCriarSimulacaoEspecifica() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/simulacoes/cliente/{clienteId}/simulacao-especifica",
                clienteTeste.getId()));

        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cliente.id", is(clienteTeste.getId().intValue())))
                .andExpect(jsonPath("$.valorSolicitado", is(300000.00)))
                .andExpect(jsonPath("$.valorGarantia", is(1000000.00)))
                .andExpect(jsonPath("$.quantidadeMeses", is(150)))
                .andExpect(jsonPath("$.taxaJurosMensal", is(2.0)));
    }

    @Test
    void testCriarSimulacaoEspecificaClienteInexistente() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/simulacoes/cliente/{clienteId}/simulacao-especifica", 999L));

        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testListarSimulacoesPorClienteComPaginacao() throws Exception {
        for (int i = 0; i < 5; i++) {
            Simulacao sim = new Simulacao();
            sim.setCliente(clienteTeste);
            sim.setDataHora(LocalDateTime.now());
            sim.setValorSolicitado(new BigDecimal("100000.00"));
            sim.setValorGarantia(new BigDecimal("200000.00"));
            sim.setQuantidadeMeses(12);
            sim.setTaxaJurosMensal(new BigDecimal("1.5"));
            simulacaoRepository.save(sim);
        }

        ResultActions response = mockMvc.perform(get("/api/simulacoes/cliente/{clienteId}", clienteTeste.getId())
                .param("page", "0")
                .param("size", "3")
                .param("sortBy", "dataHora")
                .param("direction", "desc"));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(3)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(3)));
    }

    @Test
    void testListarSimulacoesPorClienteSemPaginacao() throws Exception {
        for (int i = 0; i < 3; i++) {
            Simulacao sim = new Simulacao();
            sim.setCliente(clienteTeste);
            sim.setDataHora(LocalDateTime.now());
            sim.setValorSolicitado(new BigDecimal("100000.00"));
            sim.setValorGarantia(new BigDecimal("200000.00"));
            sim.setQuantidadeMeses(12);
            sim.setTaxaJurosMensal(new BigDecimal("1.5"));
            simulacaoRepository.save(sim);
        }

        ResultActions response = mockMvc.perform(get("/api/simulacoes/cliente/{clienteId}/all", clienteTeste.getId()));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(3)));
    }

    @Test
    void testListarSimulacoesPorClienteInexistente() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/simulacoes/cliente/{clienteId}", 999L));

        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testBuscarSimulacaoPorId() throws Exception {
        Simulacao sim = new Simulacao();
        sim.setCliente(clienteTeste);
        sim.setDataHora(LocalDateTime.now());
        sim.setValorSolicitado(new BigDecimal("150000.00"));
        sim.setValorGarantia(new BigDecimal("300000.00"));
        sim.setQuantidadeMeses(24);
        sim.setTaxaJurosMensal(new BigDecimal("1.75"));

        Simulacao savedSim = simulacaoRepository.save(sim);

        ResultActions response = mockMvc.perform(get("/api/simulacoes/{id}", savedSim.getId()));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedSim.getId().intValue())))
                .andExpect(jsonPath("$.valorSolicitado", is(150000.00)))
                .andExpect(jsonPath("$.quantidadeMeses", is(24)));
    }

    @Test
    void testBuscarSimulacaoPorIdInexistente() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/simulacoes/{id}", 999L));

        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testListarTodasSimulacoes() throws Exception {
        Simulacao sim1 = new Simulacao();
        sim1.setCliente(clienteTeste);
        sim1.setDataHora(LocalDateTime.now());
        sim1.setValorSolicitado(new BigDecimal("100000.00"));
        sim1.setValorGarantia(new BigDecimal("200000.00"));
        sim1.setQuantidadeMeses(12);
        sim1.setTaxaJurosMensal(new BigDecimal("1.5"));
        simulacaoRepository.save(sim1);

        Simulacao sim2 = new Simulacao();
        sim2.setCliente(clienteTeste);
        sim2.setDataHora(LocalDateTime.now());
        sim2.setValorSolicitado(new BigDecimal("200000.00"));
        sim2.setValorGarantia(new BigDecimal("400000.00"));
        sim2.setQuantidadeMeses(24);
        sim2.setTaxaJurosMensal(new BigDecimal("1.75"));
        simulacaoRepository.save(sim2);

        ResultActions response = mockMvc.perform(get("/api/simulacoes"));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));
    }

    @Test
    void testExportarTxt() throws Exception {
        Simulacao sim = new Simulacao();
        sim.setCliente(clienteTeste);
        sim.setDataHora(LocalDateTime.of(2024, 6, 15, 10, 30, 26));
        sim.setValorSolicitado(new BigDecimal("300000.00"));
        sim.setValorGarantia(new BigDecimal("1000000.00"));
        sim.setQuantidadeMeses(150);
        sim.setTaxaJurosMensal(new BigDecimal("2.00"));
        simulacaoRepository.save(sim);

        ResultActions response = mockMvc.perform(get("/api/simulacoes/cliente/{clienteId}/export/txt",
                clienteTeste.getId()));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"simulacoes_cliente_" + clienteTeste.getId() + ".txt\""));
    }

    @Test
    void testExportarCsv() throws Exception {
        Simulacao sim = new Simulacao();
        sim.setCliente(clienteTeste);
        sim.setDataHora(LocalDateTime.of(2024, 6, 15, 10, 30, 26));
        sim.setValorSolicitado(new BigDecimal("300000.00"));
        sim.setValorGarantia(new BigDecimal("1000000.00"));
        sim.setQuantidadeMeses(150);
        sim.setTaxaJurosMensal(new BigDecimal("2.00"));
        simulacaoRepository.save(sim);

        ResultActions response = mockMvc.perform(get("/api/simulacoes/cliente/{clienteId}/export/csv",
                clienteTeste.getId()));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"simulacoes_cliente_" + clienteTeste.getId() + ".csv\""));
    }

    @Test
    void testExportarCsvCompacto() throws Exception {
        Simulacao sim = new Simulacao();
        sim.setCliente(clienteTeste);
        sim.setDataHora(LocalDateTime.of(2024, 6, 15, 10, 30, 26));
        sim.setValorSolicitado(new BigDecimal("300000.00"));
        sim.setValorGarantia(new BigDecimal("1000000.00"));
        sim.setQuantidadeMeses(150);
        sim.setTaxaJurosMensal(new BigDecimal("2.00"));
        simulacaoRepository.save(sim);

        ResultActions response = mockMvc.perform(get("/api/simulacoes/cliente/{clienteId}/export/csv-compact",
                clienteTeste.getId()));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"simulacoes_cliente_" + clienteTeste.getId() + "_compact.csv\""));
    }

    @Test
    void testExportarTxtSemSimulacoes() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/simulacoes/cliente/{clienteId}/export/txt",
                clienteTeste.getId()));

        response.andDo(print())
                .andExpect(status().isNoContent());
    }
}