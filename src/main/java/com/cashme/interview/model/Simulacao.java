package com.cashme.interview.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "simulacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "cliente")
@EqualsAndHashCode(exclude = "cliente")
public class Simulacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "valor_solicitado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorSolicitado;

    @Column(name = "valor_garantia", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorGarantia;

    @Column(name = "quantidade_meses", nullable = false)
    private Integer quantidadeMeses;

    @Column(name = "taxa_juros_mensal", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxaJurosMensal;

    public Simulacao(Cliente cliente, LocalDateTime dataHora, BigDecimal valorSolicitado,
                     BigDecimal valorGarantia, Integer quantidadeMeses, BigDecimal taxaJurosMensal) {
        this.cliente = cliente;
        this.dataHora = dataHora;
        this.valorSolicitado = valorSolicitado;
        this.valorGarantia = valorGarantia;
        this.quantidadeMeses = quantidadeMeses;
        this.taxaJurosMensal = taxaJurosMensal;
    }
}