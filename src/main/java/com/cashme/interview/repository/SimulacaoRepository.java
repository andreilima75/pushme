package com.cashme.interview.repository;

import com.cashme.interview.model.Simulacao;
import com.cashme.interview.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SimulacaoRepository extends JpaRepository<Simulacao, Long> {

    List<Simulacao> findByCliente(Cliente cliente);

    List<Simulacao> findByClienteId(Long clienteId);

    Page<Simulacao> findByClienteId(Long clienteId, Pageable pageable);

    List<Simulacao> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT s FROM Simulacao s WHERE s.cliente.id = :clienteId ORDER BY s.dataHora DESC")
    List<Simulacao> findUltimasSimulacoesByCliente(@Param("clienteId") Long clienteId);

    @Query("SELECT s FROM Simulacao s WHERE s.valorSolicitado >= :valorMinimo")
    List<Simulacao> findByValorSolicitadoMaiorQue(@Param("valorMinimo") BigDecimal valorMinimo);
}