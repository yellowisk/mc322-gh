package br.unicamp.ic.mc322.model.demand;

import br.unicamp.ic.mc322.exception.QuantidadeDemandaInvalidaException;
import br.unicamp.ic.mc322.model.product.CopoDeVidro;
import br.unicamp.ic.mc322.model.product.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Demanda#restaurar}
 */
@DisplayName("Restauração de demandas")
class DemandaRestauracaoTest {
    private Produto copo;

    @BeforeEach
    void criarModelo() {
        copo = new CopoDeVidro("Copo", 0.5);
    }

    @Test
    @DisplayName("Demanda cancelada volta c/ quantidade, progresso, tempo, ordem e matéria-prima")
    void restaurarCancelada() {
        Demanda d = Demanda.restaurar(copo, 30, 12, StatusDemanda.CANCELADA, 131.4, 7);

        assertEquals("Copo", d.getNomeProduto());
        assertEquals(30, d.getQuantidade());
        assertEquals(12, d.getQuantidadeProduzida());
        assertEquals(18, d.getQuantidadeRestante());
        assertEquals(StatusDemanda.CANCELADA, d.getStatus());
        assertEquals(131.4, d.getTempoTotalProducao());
        assertEquals(7, d.getOrdemChegada());
        assertEquals(15.0, d.getMateriaPrimaTotal());
    }

    @Test
    @DisplayName("Demanda pendente sem progresso é aceita")
    void restaurarPendente() {
        Demanda d = Demanda.restaurar(copo, 5, 0, StatusDemanda.PENDENTE, 0, 0);

        assertEquals(StatusDemanda.PENDENTE, d.getStatus());
    }

    @Test
    @DisplayName("Demanda concluída c/ tudo produzido é aceita")
    void restaurarConcluida() {
        Demanda d = Demanda.restaurar(copo, 5, 5, StatusDemanda.CONCLUIDA, 50, 0);

        assertEquals(0, d.getQuantidadeRestante());
    }

    @Test
    @DisplayName("Demanda zerada e concluída é aceita (0 de 0)")
    void restaurarZeradaConcluida() {
        Demanda d = Demanda.restaurar(copo, 0, 0, StatusDemanda.CONCLUIDA, 0, 0);

        assertEquals(StatusDemanda.CONCLUIDA, d.getStatus());
    }

    @Test
    @DisplayName("Demanda restaurada pode voltar a ser fabricada pelas transições normais")
    void restauradaSegueAsTransicoes() {
        Demanda d = Demanda.restaurar(copo, 30, 12, StatusDemanda.CANCELADA, 131.4, 7);

        d.iniciarProducao();
        d.concluir(18, 10);

        assertEquals(StatusDemanda.CONCLUIDA, d.getStatus());
        assertEquals(30, d.getQuantidadeProduzida());
    }

    @Test
    @DisplayName("Demanda salva como Em Produção lança IllegalArgumentException")
    void emProducaoLanca() {
        assertThrows(IllegalArgumentException.class,
                () -> Demanda.restaurar(copo, 5, 2, StatusDemanda.EM_PRODUCAO, 10, 0));
    }

    @Test
    @DisplayName("Demanda pendente c/ unidades produzidas lança IllegalArgumentException")
    void pendenteComProgressoLanca() {
        assertThrows(IllegalArgumentException.class,
                () -> Demanda.restaurar(copo, 5, 1, StatusDemanda.PENDENTE, 0, 0));
    }

    @Test
    @DisplayName("Demanda pendente c/ tempo de produção lança IllegalArgumentException")
    void pendenteComTempoLanca() {
        assertThrows(IllegalArgumentException.class,
                () -> Demanda.restaurar(copo, 5, 0, StatusDemanda.PENDENTE, 3.5, 0));
    }

    @Test
    @DisplayName("Demanda cancelada c/ tudo produzido lança IllegalArgumentException")
    void canceladaCompletaLanca() {
        assertThrows(IllegalArgumentException.class,
                () -> Demanda.restaurar(copo, 5, 5, StatusDemanda.CANCELADA, 10, 0));
    }

    @Test
    @DisplayName("Demanda concluída faltando unidades lança IllegalArgumentException")
    void concluidaIncompletaLanca() {
        assertThrows(IllegalArgumentException.class,
                () -> Demanda.restaurar(copo, 5, 4, StatusDemanda.CONCLUIDA, 10, 0));
    }

    @Test
    @DisplayName("Mais unidades produzidas que a quantidade lança IllegalArgumentException")
    void produzidaMaiorQueQuantidadeLanca() {
        assertThrows(IllegalArgumentException.class,
                () -> Demanda.restaurar(copo, 5, 6, StatusDemanda.CONCLUIDA, 10, 0));
    }

    @Test
    @DisplayName("Quantidade negativa lança QuantidadeDemandaInvalidaException")
    void quantidadeNegativaLanca() {
        assertThrows(QuantidadeDemandaInvalidaException.class,
                () -> Demanda.restaurar(copo, -1, 0, StatusDemanda.PENDENTE, 0, 0));
    }

    @Test
    @DisplayName("Tempo negativo lança IllegalArgumentException")
    void tempoNegativoLanca() {
        assertThrows(IllegalArgumentException.class,
                () -> Demanda.restaurar(copo, 5, 2, StatusDemanda.CANCELADA, -1, 0));
    }

    @Test
    @DisplayName("Ordem de chegada negativa lança IllegalArgumentException")
    void ordemNegativaLanca() {
        assertThrows(IllegalArgumentException.class,
                () -> Demanda.restaurar(copo, 5, 0, StatusDemanda.PENDENTE, 0, -1));
    }

    @Test
    @DisplayName("Restaurar não mexe no contador de chegada das demandas novas")
    void restaurarNaoMexeNoContador() {
        long antes = new Demanda(copo, 1).getOrdemChegada();

        Demanda.restaurar(copo, 5, 0, StatusDemanda.PENDENTE, 0, 999);
        long depois = new Demanda(copo, 1).getOrdemChegada();

        assertEquals(antes + 1, depois);
    }
}
