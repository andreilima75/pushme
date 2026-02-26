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

        atualizaEndereco(clienteAtualizado, clienteExistente);

        return clienteRepository.save(clienteExistente);
    }

    @Transactional
    public void deletarCliente(Long id) {
        log.info("Deletando cliente ID: {}", id);

        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
        log.info("Cliente deletado com sucesso: {}", cliente.getNome());
    }

    private static void atualizaEndereco(Cliente clienteAtualizado, Cliente clienteExistente) {
        if (clienteAtualizado.getEndereco() != null) {
            if (clienteExistente.getEndereco() != null) {
                Endereco endereco = clienteExistente.getEndereco();
                endereco.setRua(clienteAtualizado.getEndereco().getRua());
                endereco.setNumero(clienteAtualizado.getEndereco().getNumero());
                endereco.setBairro(clienteAtualizado.getEndereco().getBairro());
                endereco.setCep(clienteAtualizado.getEndereco().getCep());
                endereco.setCidade(clienteAtualizado.getEndereco().getCidade());
                endereco.setEstado(clienteAtualizado.getEndereco().getEstado());
            } else {
                Endereco novoEndereco = clienteAtualizado.getEndereco();
                novoEndereco.setCliente(clienteExistente);
                clienteExistente.setEndereco(novoEndereco);
            }
        }
    }

    /**
     * Calcula uma lista com os nomes dos clientes que moram em uma determinada cidade e estado.
     *
     * @param clientes lista dos clientes usados como base para o cálculo
     * @param cidade cidade dos clientes a serem recuperados
     * @param estado estado da cidade dos clientes a serem recuperados
     * @return Lista com os nomes dos clientes da lista fornecida que moram na cidade e estado fornecida.
     */
    public List<String> calculaNomesClientesParaCidadeEstado(List<Cliente> clientes, String cidade, String estado) {
        return clientes.stream().filter(c -> cidade.equalsIgnoreCase(c.getEndereco().getCidade())
                && estado.equalsIgnoreCase(c.getEndereco().getEstado())).map(Cliente::getNome)
                .toList();
    }

}