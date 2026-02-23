package com.cashme.interview.service;

import com.cashme.interview.model.Cliente;
import com.cashme.interview.model.Endereco;
import com.cashme.interview.repository.ClienteRepository;
import com.cashme.interview.exception.ResourceNotFoundException;
import com.cashme.interview.exception.DuplicateCpfException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public Cliente criarCliente(Cliente cliente) {
        log.info("Criando novo cliente: {}", cliente.getNome());

        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new DuplicateCpfException("CPF já cadastrado: " + cliente.getCpf());
        }

        if (cliente.getEndereco() != null) {
            cliente.getEndereco().setCliente(cliente);
        }

        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        log.info("Listando todos os clientes");
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        log.info("Buscando cliente por ID: {}", id);
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorCpf(String cpf) {
        log.info("Buscando cliente por CPF: {}", cpf);
        return clienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com CPF: " + cpf));
    }

    public Cliente atualizarCliente(Long id, Cliente clienteAtualizado) {
        log.info("Atualizando cliente ID: {}", id);

        Cliente clienteExistente = buscarPorId(id);

        if (!clienteExistente.getCpf().equals(clienteAtualizado.getCpf()) &&
                clienteRepository.existsByCpf(clienteAtualizado.getCpf())) {
            throw new DuplicateCpfException("CPF já cadastrado: " + clienteAtualizado.getCpf());
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

    public Cliente atualizarClienteParcial(Long id, Cliente clienteAtualizado) {
        log.info("Atualização parcial do cliente ID: {}", id);

        Cliente clienteExistente = buscarPorId(id);

        if (clienteAtualizado.getNome() != null) {
            clienteExistente.setNome(clienteAtualizado.getNome());
        }

        if (clienteAtualizado.getCpf() != null) {
            if (!clienteExistente.getCpf().equals(clienteAtualizado.getCpf()) &&
                    clienteRepository.existsByCpf(clienteAtualizado.getCpf())) {
                throw new DuplicateCpfException("CPF já cadastrado: " + clienteAtualizado.getCpf());
            }
            clienteExistente.setCpf(clienteAtualizado.getCpf());
        }

        if (clienteAtualizado.getEndereco() != null) {
            Endereco enderecoAtualizado = clienteAtualizado.getEndereco();
            enderecoAtualizado.setCliente(clienteExistente);
            clienteExistente.setEndereco(enderecoAtualizado);
        }

        return clienteRepository.save(clienteExistente);
    }

    public void deletarCliente(Long id) {
        log.info("Deletando cliente ID: {}", id);

        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
        log.info("Cliente deletado com sucesso: {}", cliente.getNome());
    }

    public void deletarTodos() {
        log.warn("Deletando TODOS os clientes!");
        clienteRepository.deleteAll();
    }

    public boolean clienteExiste(Long id) {
        return clienteRepository.existsById(id);
    }

    public long contarClientes() {
        return clienteRepository.count();
    }
}