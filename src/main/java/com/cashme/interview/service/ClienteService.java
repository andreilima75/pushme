package com.cashme.interview.service;

import com.cashme.interview.model.Cliente;
import com.cashme.interview.model.Endereco;
import com.cashme.interview.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public Cliente criarCliente(Cliente cliente) {
        log.info("Criando novo cliente: {}", cliente.getNome());

        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "CPF já cadastrado: " + cliente.getCpf()
            );
        }

        if (cliente.getEndereco() != null) {
            cliente.getEndereco().setCliente(cliente);
        }

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        log.info("Listando todos os clientes");
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        log.info("Buscando cliente por ID: {}", id);
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente não encontrado com ID: " + id
                ));
    }

    @Transactional
    public Cliente atualizarCliente(Long id, Cliente clienteAtualizado) {
        log.info("Atualizando cliente ID: {}", id);

        Cliente clienteExistente = buscarPorId(id);

        if (!clienteExistente.getCpf().equals(clienteAtualizado.getCpf()) &&
                clienteRepository.existsByCpf(clienteAtualizado.getCpf())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "CPF já cadastrado: " + clienteAtualizado.getCpf()
            );
        }

        clienteExistente.setCpf(clienteAtualizado.getCpf());
        clienteExistente.setNome(clienteAtualizado.getNome());

        if (clienteAtualizado.getEndereco() != null) {
            Endereco enderecoAtualizado = clienteAtualizado.getEndereco();
            enderecoAtualizado.setCliente(clienteExistente);
            clienteExistente.setEndereco(enderecoAtualizado);
        }

        return clienteRepository.save(clienteExistente);
    }

    @Transactional
    public void deletarCliente(Long id) {
        log.info("Deletando cliente ID: {}", id);

        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
        log.info("Cliente deletado com sucesso: {}", cliente.getNome());
    }

    @Transactional
    public void deletarTodos() {
        log.warn("Deletando TODOS os clientes!");
        clienteRepository.deleteAll();
    }

}