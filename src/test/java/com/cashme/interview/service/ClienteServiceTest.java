package com.cashme.interview.service;

import com.cashme.interview.model.Cliente;
import com.cashme.interview.model.Endereco;
import com.cashme.interview.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService service;

    @Test
    void filtrarClientes(){
        Endereco endereco1 = new Endereco();
        endereco1.setCidade("Curitiba");
        endereco1.setEstado("PR");

        Cliente cliente1 = new Cliente();
        cliente1.setNome("Joao");
        cliente1.setEndereco(endereco1);

        Endereco endereco2 = new Endereco();
        endereco2.setCidade("Sao Paulo");
        endereco2.setEstado("SP");

        Cliente cliente2 = new Cliente();
        cliente2.setNome("Maria");
        cliente2.setEndereco(endereco2);

        List<Cliente> lista = List.of(cliente1, cliente2);

        List<String> resultado = service.calculaNomesClientesParaCidadeEstado(lista, "Curitiba", "PR");

        assertThat(resultado).hasSize(1).containsExactly("Joao");
    }

}