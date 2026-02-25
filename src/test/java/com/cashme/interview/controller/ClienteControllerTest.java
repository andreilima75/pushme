package com.cashme.interview.controller;

import com.cashme.interview.model.Cliente;
import com.cashme.interview.model.Endereco;
import com.cashme.interview.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        clienteRepository.deleteAll();
    }

    @Test
    void testCriarCliente() throws Exception {
        Endereco endereco = new Endereco();
        endereco.setRua("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Centro");
        endereco.setCep("12345-678");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");

        Cliente cliente = new Cliente();
        cliente.setCpf("12345678901");
        cliente.setNome("João Teste");
        cliente.setEndereco(endereco);

        ResultActions response = mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)));

        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome", is("João Teste")))
                .andExpect(jsonPath("$.cpf", is("12345678901")))
                .andExpect(jsonPath("$.endereco.rua", is("Rua Teste")));
    }

    @Test
    void testCriarClienteComCpfDuplicado() throws Exception {
        Endereco endereco1 = new Endereco();
        endereco1.setRua("Rua Teste 1");
        endereco1.setNumero("123");
        endereco1.setBairro("Centro");
        endereco1.setCep("12345-678");
        endereco1.setCidade("São Paulo");
        endereco1.setEstado("SP");

        Cliente cliente1 = new Cliente();
        cliente1.setCpf("12345678901");
        cliente1.setNome("João Teste");
        cliente1.setEndereco(endereco1);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente1)))
                .andExpect(status().isCreated());

        Endereco endereco2 = new Endereco();
        endereco2.setRua("Rua Teste 2");
        endereco2.setNumero("456");
        endereco2.setBairro("Jardins");
        endereco2.setCep("87654-321");
        endereco2.setCidade("São Paulo");
        endereco2.setEstado("SP");

        Cliente cliente2 = new Cliente();
        cliente2.setCpf("12345678901");
        cliente2.setNome("Maria Teste");
        cliente2.setEndereco(endereco2);

        ResultActions response = mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente2)));

        response.andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void testListarTodosClientes() throws Exception {
        Endereco endereco1 = new Endereco();
        endereco1.setRua("Rua A");
        endereco1.setNumero("100");
        endereco1.setBairro("Centro");
        endereco1.setCep("11111-111");
        endereco1.setCidade("São Paulo");
        endereco1.setEstado("SP");

        Cliente cliente1 = new Cliente();
        cliente1.setCpf("11111111111");
        cliente1.setNome("Cliente A");
        cliente1.setEndereco(endereco1);
        clienteRepository.save(cliente1);

        Endereco endereco2 = new Endereco();
        endereco2.setRua("Rua B");
        endereco2.setNumero("200");
        endereco2.setBairro("Jardins");
        endereco2.setCep("22222-222");
        endereco2.setCidade("São Paulo");
        endereco2.setEstado("SP");

        Cliente cliente2 = new Cliente();
        cliente2.setCpf("22222222222");
        cliente2.setNome("Cliente B");
        cliente2.setEndereco(endereco2);
        clienteRepository.save(cliente2);

        ResultActions response = mockMvc.perform(get("/api/clientes"));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].nome").exists())
                .andExpect(jsonPath("$[1].nome").exists());
    }

    @Test
    void testBuscarClientePorId() throws Exception {
        Endereco endereco = new Endereco();
        endereco.setRua("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Centro");
        endereco.setCep("12345-678");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");

        Cliente cliente = new Cliente();
        cliente.setCpf("12345678901");
        cliente.setNome("João Teste");
        cliente.setEndereco(endereco);

        Cliente savedCliente = clienteRepository.save(cliente);

        ResultActions response = mockMvc.perform(get("/api/clientes/{id}", savedCliente.getId()));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedCliente.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("João Teste")))
                .andExpect(jsonPath("$.cpf", is("12345678901")));
    }

    @Test
    void testBuscarClientePorIdInexistente() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/clientes/{id}", 999L));

        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testBuscarClientePorCpf() throws Exception {
        Endereco endereco = new Endereco();
        endereco.setRua("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Centro");
        endereco.setCep("12345-678");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");

        Cliente cliente = new Cliente();
        cliente.setCpf("12345678901");
        cliente.setNome("João Teste");
        cliente.setEndereco(endereco);

        clienteRepository.save(cliente);

        ResultActions response = mockMvc.perform(get("/api/clientes/cpf/{cpf}", "12345678901"));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("João Teste")))
                .andExpect(jsonPath("$.cpf", is("12345678901")));
    }

    @Test
    void testBuscarClientePorCpfInexistente() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/clientes/cpf/{cpf}", "00000000000"));

        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testAtualizarCliente() throws Exception {
        Endereco endereco = new Endereco();
        endereco.setRua("Rua Antiga");
        endereco.setNumero("100");
        endereco.setBairro("Centro");
        endereco.setCep("11111-111");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");

        Cliente cliente = new Cliente();
        cliente.setCpf("11111111111");
        cliente.setNome("Cliente Antigo");
        cliente.setEndereco(endereco);

        Cliente savedCliente = clienteRepository.save(cliente);

        Endereco novoEndereco = new Endereco();
        novoEndereco.setRua("Rua Nova");
        novoEndereco.setNumero("200");
        novoEndereco.setBairro("Jardins");
        novoEndereco.setCep("22222-222");
        novoEndereco.setCidade("São Paulo");
        novoEndereco.setEstado("SP");

        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setCpf("11111111111");
        clienteAtualizado.setNome("Cliente Atualizado");
        clienteAtualizado.setEndereco(novoEndereco);

        ResultActions response = mockMvc.perform(put("/api/clientes/{id}", savedCliente.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteAtualizado)));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Cliente Atualizado")))
                .andExpect(jsonPath("$.endereco.rua", is("Rua Nova")));
    }

    @Test
    void testAtualizarParcialCliente() throws Exception {
        Endereco endereco = new Endereco();
        endereco.setRua("Rua Original");
        endereco.setNumero("100");
        endereco.setBairro("Centro");
        endereco.setCep("11111-111");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");

        Cliente cliente = new Cliente();
        cliente.setCpf("11111111111");
        cliente.setNome("Cliente Original");
        cliente.setEndereco(endereco);

        Cliente savedCliente = clienteRepository.save(cliente);

        Cliente clienteParcial = new Cliente();
        clienteParcial.setNome("Nome Alterado Apenas");

        ResultActions response = mockMvc.perform(patch("/api/clientes/{id}", savedCliente.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteParcial)));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Nome Alterado Apenas")))
                .andExpect(jsonPath("$.cpf", is("11111111111")))
                .andExpect(jsonPath("$.endereco.rua", is("Rua Original")));
    }

    @Test
    void testDeletarCliente() throws Exception {
        Endereco endereco = new Endereco();
        endereco.setRua("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Centro");
        endereco.setCep("12345-678");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");

        Cliente cliente = new Cliente();
        cliente.setCpf("12345678901");
        cliente.setNome("João Teste");
        cliente.setEndereco(endereco);

        Cliente savedCliente = clienteRepository.save(cliente);

        ResultActions response = mockMvc.perform(delete("/api/clientes/{id}", savedCliente.getId()));

        response.andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletarClienteInexistente() throws Exception {
        ResultActions response = mockMvc.perform(delete("/api/clientes/{id}", 999L));

        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testClienteExiste() throws Exception {
        Endereco endereco = new Endereco();
        endereco.setRua("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Centro");
        endereco.setCep("12345-678");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");

        Cliente cliente = new Cliente();
        cliente.setCpf("12345678901");
        cliente.setNome("João Teste");
        cliente.setEndereco(endereco);

        Cliente savedCliente = clienteRepository.save(cliente);

        ResultActions response = mockMvc.perform(get("/api/clientes/{id}/exists", savedCliente.getId()));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testContarClientes() throws Exception {
        Endereco endereco1 = new Endereco();
        endereco1.setRua("Rua A");
        endereco1.setNumero("100");
        endereco1.setBairro("Centro");
        endereco1.setCep("11111-111");
        endereco1.setCidade("São Paulo");
        endereco1.setEstado("SP");

        Cliente cliente1 = new Cliente();
        cliente1.setCpf("11111111111");
        cliente1.setNome("Cliente A");
        cliente1.setEndereco(endereco1);
        clienteRepository.save(cliente1);

        Endereco endereco2 = new Endereco();
        endereco2.setRua("Rua B");
        endereco2.setNumero("200");
        endereco2.setBairro("Jardins");
        endereco2.setCep("22222-222");
        endereco2.setCidade("São Paulo");
        endereco2.setEstado("SP");

        Cliente cliente2 = new Cliente();
        cliente2.setCpf("22222222222");
        cliente2.setNome("Cliente B");
        cliente2.setEndereco(endereco2);
        clienteRepository.save(cliente2);

        ResultActions response = mockMvc.perform(get("/api/clientes/count"));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }
}