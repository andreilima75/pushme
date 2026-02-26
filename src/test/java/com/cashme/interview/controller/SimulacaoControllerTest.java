package com.cashme.interview.controller;

import com.cashme.interview.model.Cliente;
import com.cashme.interview.model.Simulacao;
import com.cashme.interview.service.SimulacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulacaoControllerTest {

    @Mock
    private SimulacaoService simulacaoService;

    @InjectMocks
    private SimulacaoController simulacaoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Cliente cliente;
    private Simulacao simulacao1;
    private Simulacao simulacao2;
    private LocalDateTime dataHora;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(simulacaoController).build();
        objectMapper = new ObjectMapper();

        dataHora = LocalDateTime.of(2024, 6, 15, 10, 30, 26);

        // Setup Cliente
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setCpf("12345678900");
        cliente.setNome("João Silva");

        // Setup Simulação 1
        simulacao1 = new Simulacao();
        simulacao1.setId(1L);
        simulacao1.setCliente(cliente);
        simulacao1.setDataHora(dataHora);
        simulacao1.setValorSolicitado(new BigDecimal("300000.00"));
        simulacao1.setValorGarantia(new BigDecimal("1000000.00"));
        simulacao1.setQuantidadeMeses(150);
        simulacao1.setTaxaJurosMensal(new BigDecimal("2.00"));

        // Setup Simulação 2
        simulacao2 = new Simulacao();
        simulacao2.setId(2L);
        simulacao2.setCliente(cliente);
        simulacao2.setDataHora(dataHora.plusDays(1));
        simulacao2.setValorSolicitado(new BigDecimal("500000.00"));
        simulacao2.setValorGarantia(new BigDecimal("1500000.00"));
        simulacao2.setQuantidadeMeses(180);
        simulacao2.setTaxaJurosMensal(new BigDecimal("1.85"));
    }

    @Test
    void listarPorCliente_DeveRetornarPageDeSimulacoes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataHora"));
        Page<Simulacao> page = new PageImpl<>(Arrays.asList(simulacao1, simulacao2), pageable, 2);

        when(simulacaoService.listarPorCliente(eq(1L), any(Pageable.class))).thenReturn(page);

        // When
        ResponseEntity<Page<Simulacao>> response = simulacaoController.listarPorCliente(1L, 0, 10, "dataHora", "desc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        assertThat(response.getBody().getContent().get(0).getId()).isEqualTo(1L);
        assertThat(response.getBody().getContent().get(1).getId()).isEqualTo(2L);

        verify(simulacaoService, times(1)).listarPorCliente(eq(1L), any(Pageable.class));
    }

    @Test
    void listarPorCliente_ComParametrosPersonalizados_DeveCriarPageableCorreto() {
        // Given
        Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "valorSolicitado"));
        Page<Simulacao> page = new PageImpl<>(List.of(simulacao1), pageable, 1);

        when(simulacaoService.listarPorCliente(eq(1L), any(Pageable.class))).thenReturn(page);

        // When
        ResponseEntity<Page<Simulacao>> response = simulacaoController.listarPorCliente(1L, 1, 5, "valorSolicitado", "asc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);

        verify(simulacaoService, times(1)).listarPorCliente(eq(1L), any(Pageable.class));
    }

    @Test
    void exportarTxt_ComSimulacoes_DeveRetornarArquivoTxt() {
        // Given
        List<Simulacao> simulacoes = Arrays.asList(simulacao1, simulacao2);
        String relatorioEsperado = "RELATÓRIO DE SIMULAÇÕES\n" +
                "========================\n\n" +
                "Cliente: João Silva\n" +
                "CPF: 12345678900\n" +
                "Total de simulações: 2\n\n";

        when(simulacaoService.buscarPorClienteId(1L)).thenReturn(simulacoes);
        when(simulacaoService.gerarRelatorioTxt(simulacoes)).thenReturn(relatorioEsperado);

        // When
        ResponseEntity<String> response = simulacaoController.exportarTxt(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("filename=\"simulacoes_cliente_1.txt\"");
        assertThat(response.getBody()).isEqualTo(relatorioEsperado);

        verify(simulacaoService, times(1)).buscarPorClienteId(1L);
        verify(simulacaoService, times(1)).gerarRelatorioTxt(simulacoes);
    }

    @Test
    void exportarTxt_SemSimulacoes_DeveRetornarNoContent() {
        // Given
        when(simulacaoService.buscarPorClienteId(1L)).thenReturn(List.of());

        // When
        ResponseEntity<String> response = simulacaoController.exportarTxt(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(simulacaoService, times(1)).buscarPorClienteId(1L);
        verify(simulacaoService, never()).gerarRelatorioTxt(anyList());
    }

    @Test
    void exportarCsv_ComSimulacoes_DeveRetornarArquivoCsv() {
        // Given
        List<Simulacao> simulacoes = Arrays.asList(simulacao1, simulacao2);
        String relatorioEsperado = "ID,Data,Hora,ValorSolicitado,ValorGarantia,Meses,TaxaJuros,ClienteID,ClienteNome,ClienteCPF\n" +
                "1,15/06/2024,10:30:26,300000.00,1000000.00,150,2.00,1,\"João Silva\",12345678900\n" +
                "2,16/06/2024,10:30:26,500000.00,1500000.00,180,1.85,1,\"João Silva\",12345678900\n";

        when(simulacaoService.buscarPorClienteId(1L)).thenReturn(simulacoes);
        when(simulacaoService.gerarRelatorioCsv(simulacoes)).thenReturn(relatorioEsperado);

        // When
        ResponseEntity<String> response = simulacaoController.exportarCsv(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("text/csv"));
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("filename=\"simulacoes_cliente_1.csv\"");
        assertThat(response.getBody()).isEqualTo(relatorioEsperado);

        verify(simulacaoService, times(1)).buscarPorClienteId(1L);
        verify(simulacaoService, times(1)).gerarRelatorioCsv(simulacoes);
    }

    @Test
    void exportarCsv_SemSimulacoes_DeveRetornarNoContent() {
        // Given
        when(simulacaoService.buscarPorClienteId(1L)).thenReturn(List.of());

        // When
        ResponseEntity<String> response = simulacaoController.exportarCsv(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(simulacaoService, times(1)).buscarPorClienteId(1L);
        verify(simulacaoService, never()).gerarRelatorioCsv(anyList());
    }

    @Test
    void listarTodas_DeveRetornarLista() {
        // Given
        List<Simulacao> simulacoes = Arrays.asList(simulacao1, simulacao2);
        when(simulacaoService.listarTodas()).thenReturn(simulacoes);

        // When
        List<Simulacao> resultado = simulacaoController.listarTodas();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
        assertThat(resultado.get(1).getId()).isEqualTo(2L);

        verify(simulacaoService, times(1)).listarTodas();
    }

    @Test
    void buscarPorId_ComIdExistente_DeveRetornarSimulacao() {
        // Given
        when(simulacaoService.buscarPorId(1L)).thenReturn(simulacao1);

        // When
        ResponseEntity<Simulacao> response = simulacaoController.buscarPorId(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getValorSolicitado()).isEqualByComparingTo("300000.00");

        verify(simulacaoService, times(1)).buscarPorId(1L);
    }

    @Test
    void criarSimulacaoEspecifica_ComClienteExistente_DeveCriarESalvar() {
        // Given
        when(simulacaoService.criarSimulacaoEspecifica(1L)).thenReturn(simulacao1);

        // When
        Simulacao resultado = simulacaoController.criarSimulacaoEspecifica(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getCliente().getId()).isEqualTo(1L);
        assertThat(resultado.getDataHora()).isEqualTo(dataHora);
        assertThat(resultado.getValorSolicitado()).isEqualByComparingTo("300000.00");
        assertThat(resultado.getValorGarantia()).isEqualByComparingTo("1000000.00");
        assertThat(resultado.getQuantidadeMeses()).isEqualTo(150);
        assertThat(resultado.getTaxaJurosMensal()).isEqualByComparingTo("2.00");

        verify(simulacaoService, times(1)).criarSimulacaoEspecifica(1L);
    }

    @Test
    void listarPorCliente_ComParametrosPadrao_DeveUsarValoresDefault() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataHora"));
        Page<Simulacao> page = new PageImpl<>(Arrays.asList(simulacao1, simulacao2), pageable, 2);

        when(simulacaoService.listarPorCliente(eq(1L), any(Pageable.class))).thenReturn(page);

        // When - Usando valores padrão (0, 10, "dataHora", "desc")
        ResponseEntity<Page<Simulacao>> response = simulacaoController.listarPorCliente(1L, 0, 10, "dataHora", "desc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(simulacaoService, times(1)).listarPorCliente(eq(1L), any(Pageable.class));
    }

    @Test
    void exportarTxt_DeveConfigurarHeadersCorretamente() {
        // Given
        List<Simulacao> simulacoes = List.of(simulacao1);
        String relatorio = "conteúdo do relatório";

        when(simulacaoService.buscarPorClienteId(1L)).thenReturn(simulacoes);
        when(simulacaoService.gerarRelatorioTxt(simulacoes)).thenReturn(relatorio);

        // When
        ResponseEntity<String> response = simulacaoController.exportarTxt(1L);

        // Then
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("filename=\"simulacoes_cliente_1.txt\"");
    }

    @Test
    void exportarCsv_DeveConfigurarHeadersCorretamente() {
        // Given
        List<Simulacao> simulacoes = List.of(simulacao1);
        String relatorio = "conteúdo do CSV";

        when(simulacaoService.buscarPorClienteId(1L)).thenReturn(simulacoes);
        when(simulacaoService.gerarRelatorioCsv(simulacoes)).thenReturn(relatorio);

        // When
        ResponseEntity<String> response = simulacaoController.exportarCsv(1L);

        // Then
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("text/csv"));
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("filename=\"simulacoes_cliente_1.csv\"");
    }
}