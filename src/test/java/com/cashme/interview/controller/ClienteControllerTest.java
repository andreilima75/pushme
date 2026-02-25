package com.cashme.interview.controller;

import com.cashme.interview.model.Cliente;
import com.cashme.interview.model.Endereco;
import com.cashme.interview.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Cliente cliente;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clienteController).build();
        objectMapper = new ObjectMapper();

        // Setup Endereco
        endereco = new Endereco();
        endereco.setId(1L);
        endereco.setRua("Rua das Flores");
        endereco.setNumero("123");
        endereco.setBairro("Centro");
        endereco.setCep("12345-678");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");

        // Setup Cliente
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setCpf("12345678900");
        cliente.setNome("João Silva");
        cliente.setEndereco(endereco);
    }

    @Test
    void criarCliente_DeveRetornarClienteCriado() throws Exception {
        // Given
        when(clienteService.criarCliente(any(Cliente.class))).thenReturn(cliente);

        // When & Then
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.endereco.rua").value("Rua das Flores"));

        verify(clienteService, times(1)).criarCliente(any(Cliente.class));
    }

    @Test
    void listarTodos_DeveRetornarListaDeClientes() throws Exception {
        // Given
        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setCpf("98765432100");
        cliente2.setNome("Maria Souza");

        List<Cliente> clientes = Arrays.asList(cliente, cliente2);
        when(clienteService.listarTodos()).thenReturn(clientes);

        // When & Then
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nome").value("João Silva"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].nome").value("Maria Souza"));

        verify(clienteService, times(1)).listarTodos();
    }

    @Test
    void buscarPorId_ComIdExistente_DeveRetornarCliente() throws Exception {
        // Given
        when(clienteService.buscarPorId(1L)).thenReturn(cliente);

        // When & Then
        mockMvc.perform(get("/api/clientes/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.nome").value("João Silva"));

        verify(clienteService, times(1)).buscarPorId(1L);
    }

    @Test
    void atualizarCliente_ComIdValido_DeveRetornarClienteAtualizado() throws Exception {
        // Given
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setId(1L);
        clienteAtualizado.setCpf("12345678900");
        clienteAtualizado.setNome("João Silva Atualizado");
        clienteAtualizado.setEndereco(endereco);

        when(clienteService.atualizarCliente(eq(1L), any(Cliente.class))).thenReturn(clienteAtualizado);

        // When & Then
        mockMvc.perform(put("/api/clientes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteAtualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("João Silva Atualizado"));

        verify(clienteService, times(1)).atualizarCliente(eq(1L), any(Cliente.class));
    }

    @Test
    void deletarCliente_ComIdValido_DeveRetornarNoContent() throws Exception {
        // Given
        doNothing().when(clienteService).deletarCliente(1L);

        // When & Then
        mockMvc.perform(delete("/api/clientes/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(clienteService, times(1)).deletarCliente(1L);
    }

    @Test
    void deletarTodos_DeveRetornarNoContent() throws Exception {
        // Given
        doNothing().when(clienteService).deletarTodos();

        // When & Then
        mockMvc.perform(delete("/api/clientes"))
                .andExpect(status().isNoContent());

        verify(clienteService, times(1)).deletarTodos();
    }

}