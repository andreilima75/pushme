package com.cashme.interview.controller;

import com.cashme.interview.model.Cliente;
import com.cashme.interview.model.Simulacao;
import com.cashme.interview.repository.ClienteRepository;
import com.cashme.interview.repository.SimulacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulacaoControllerTest {

    @Mock
    private SimulacaoRepository simulacaoRepository;

    @Mock
    private ClienteRepository clienteRepository;

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
    void listarPorCliente_ComClienteInexistente_DeveLancarExcecao() {
        // Given
        when(clienteRepository.existsById(99L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> simulacaoController.listarPorCliente(99L, 0, 10, "dataHora", "desc"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("Cliente não encontrado com ID: 99");

        verify(clienteRepository, times(1)).existsById(99L);
        verify(simulacaoRepository, never()).findByClienteId(anyLong(), any(Pageable.class));
    }


    @Test
    void exportarTxt_ComClienteInexistente_DeveLancarExcecao() {
        // Given
        when(clienteRepository.existsById(99L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> simulacaoController.exportarTxt(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("Cliente não encontrado com ID: 99");

        verify(clienteRepository, times(1)).existsById(99L);
        verify(simulacaoRepository, never()).findByClienteId(anyLong());
    }

    @Test
    void exportarTxt_SemSimulacoes_DeveRetornarNoContent() {
        // Given
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(simulacaoRepository.findByClienteId(1L)).thenReturn(List.of());

        // When
        ResponseEntity<String> response = simulacaoController.exportarTxt(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(clienteRepository, times(1)).existsById(1L);
        verify(simulacaoRepository, times(1)).findByClienteId(1L);
    }


    @Test
    void exportarCsv_ComClienteInexistente_DeveLancarExcecao() {
        // Given
        when(clienteRepository.existsById(99L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> simulacaoController.exportarCsv(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("Cliente não encontrado com ID: 99");

        verify(clienteRepository, times(1)).existsById(99L);
        verify(simulacaoRepository, never()).findByClienteId(anyLong());
    }

    @Test
    void exportarCsv_SemSimulacoes_DeveRetornarNoContent() {
        // Given
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(simulacaoRepository.findByClienteId(1L)).thenReturn(List.of());

        // When
        ResponseEntity<String> response = simulacaoController.exportarCsv(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(clienteRepository, times(1)).existsById(1L);
        verify(simulacaoRepository, times(1)).findByClienteId(1L);
    }

    @Test
    void listarTodas_DeveRetornarLista() {
        // Given
        List<Simulacao> simulacoes = Arrays.asList(simulacao1, simulacao2);
        when(simulacaoRepository.findAll()).thenReturn(simulacoes);

        // When
        List<Simulacao> resultado = simulacaoController.listarTodas();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
        assertThat(resultado.get(1).getId()).isEqualTo(2L);

        verify(simulacaoRepository, times(1)).findAll();
    }

    @Test
    void buscarPorId_ComIdExistente_DeveRetornarSimulacao() {
        // Given
        when(simulacaoRepository.findById(1L)).thenReturn(Optional.of(simulacao1));

        // When
        ResponseEntity<Simulacao> response = simulacaoController.buscarPorId(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getValorSolicitado()).isEqualByComparingTo("300000.00");

        verify(simulacaoRepository, times(1)).findById(1L);
    }

    @Test
    void buscarPorId_ComIdInexistente_DeveLancarExcecao() {
        // Given
        when(simulacaoRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> simulacaoController.buscarPorId(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("Simulação não encontrada com ID: 99");

        verify(simulacaoRepository, times(1)).findById(99L);
    }

    @Test
    void criarSimulacaoEspecifica_ComClienteExistente_DeveCriarESalvar() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(simulacaoRepository.save(any(Simulacao.class))).thenReturn(simulacao1);

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

        verify(clienteRepository, times(1)).findById(1L);
        verify(simulacaoRepository, times(1)).save(any(Simulacao.class));
    }

    @Test
    void criarSimulacaoEspecifica_ComClienteInexistente_DeveLancarExcecao() {
        // Given
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> simulacaoController.criarSimulacaoEspecifica(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("Cliente não encontrado com ID: 99");

        verify(clienteRepository, times(1)).findById(99L);
        verify(simulacaoRepository, never()).save(any(Simulacao.class));
    }


    @Test
    void listarPorCliente_ComParametrosPadrao_DeveUsarValoresDefault() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataHora"));
        Page<Simulacao> page = new PageImpl<>(Arrays.asList(simulacao1, simulacao2), pageable, 2);

        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(simulacaoRepository.findByClienteId(eq(1L), any(Pageable.class))).thenReturn(page);

        // When - Usando valores padrão (0, 10, "dataHora", "desc")
        ResponseEntity<Page<Simulacao>> response = simulacaoController.listarPorCliente(1L, 0, 10, "dataHora", "desc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(clienteRepository, times(1)).existsById(1L);
        verify(simulacaoRepository, times(1)).findByClienteId(eq(1L), any(Pageable.class));
    }
}