package br.unicamp.ic.mc322.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CatalogoEstrategias")
class CatalogoEstrategiasTest {

    @Test
    @DisplayName("A estratégia padrão é a Fila do Caixa")
    void padraoEhFilaDoCaixa() {
        assertInstanceOf(FilaDoCaixa.class, CatalogoEstrategias.padrao());
    }

    @Test
    @DisplayName("Tem as três estratégias, na ordem do menu")
    void todasNaOrdemDoMenu() {
        assertEquals(3, CatalogoEstrategias.todas().size());
        assertInstanceOf(FilaDoCaixa.class, CatalogoEstrategias.todas().get(0));
        assertInstanceOf(Ferrari.class, CatalogoEstrategias.todas().get(1));
        assertInstanceOf(Joaozinho.class, CatalogoEstrategias.todas().get(2));
    }

    @Test
    @DisplayName("Buscar pelo nome devolve a mesma instância do menu")
    void buscarPorNomeDevolveAMesmaInstancia() {
        EstrategiaProducao ferrari = CatalogoEstrategias.todas().get(1);

        assertSame(ferrari, CatalogoEstrategias.buscarPorNome("Ferrari").orElseThrow());
    }

    @Test
    @DisplayName("Buscar pelo nome acha nome c/ acento e não diferencia maiúsculas")
    void buscarPorNomeComAcentoEMaiusculas() {
        assertInstanceOf(Joaozinho.class, CatalogoEstrategias.buscarPorNome("JOÃOZINHO").orElseThrow());
    }

    @Test
    @DisplayName("Buscar por um nome que não existe devolve vazio")
    void buscarPorNomeInexistenteDevolveVazio() {
        assertTrue(CatalogoEstrategias.buscarPorNome("Eitcha!").isEmpty());
    }
}
