package br.unicamp.ic.mc322.strategy;

import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.model.product.CopoDeVidro;
import br.unicamp.ic.mc322.model.product.PoteDeVidro;
import br.unicamp.ic.mc322.model.product.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes Estratégias de Produção")
public class EstrategiasTest {

    private Produto copo;
    private Produto pote;

    private Demanda demandaAntigaPequena; // Quantidade: 5,  Ordem: 0 (Chegou primeiro)
    private Demanda demandaNovaGrande;    // Quantidade: 50, Ordem: 1 (Chegou depois)
    private Demanda demandaConcluida;     // Status: CONCLUÍDA (Não selecionável)

    @BeforeEach
    void setUp() {
        copo = new CopoDeVidro("Copo de Vidro", 0.5);
        pote = new PoteDeVidro("Pote de Vidro", 1.0);

        demandaAntigaPequena = new Demanda(copo, 5);
        demandaNovaGrande = new Demanda(pote, 50);

        demandaConcluida = new Demanda(pote, 10);
        demandaConcluida.iniciarProducao();
        demandaConcluida.concluir(10, 50.0);
    }

    @Nested
    @DisplayName("Filtro Comum - Método Default selecionaveis()")
    class SelecionaveisTestes {

        @Test
        @DisplayName("Deve filtrar apenas demandas PENDENTES e rejeitar as concluídas")
        void deveFiltrarDemandasElegiveis() {
            EstrategiaProducao estrategia = new FilaDoCaixa();
            List<Demanda> todas = List.of(demandaAntigaPequena, demandaNovaGrande, demandaConcluida);

            // 1. sucesso
            List<Demanda> elegiveis = estrategia.selecionaveis(todas);
            assertEquals(2, elegiveis.size());
            assertTrue(elegiveis.contains(demandaAntigaPequena));
            assertTrue(elegiveis.contains(demandaNovaGrande));
            assertFalse(elegiveis.contains(demandaConcluida));

            // 2. falha (retorna lista vazia se nenhuma for elegível)
            List<Demanda> vazias = estrategia.selecionaveis(List.of(demandaConcluida));
            assertTrue(vazias.isEmpty());
        }
    }

    @Nested
    @DisplayName("Estratégia: Fila do Caixa (Ordem de Chegada)")
    class FilaDoCaixaTestes {

        @Test
        @DisplayName("Deve selecionar a demanda mais antiga e retornar null se vazio")
        void deveSelecionarPrimeiraDemanda() {
            FilaDoCaixa filaDoCaixa = new FilaDoCaixa();
            List<Demanda> demandas = List.of(demandaNovaGrande, demandaAntigaPequena);

            // 1. sucesso
            Demanda escolhida = filaDoCaixa.selecionarDemanda(demandas, 0.0);
            assertNotNull(escolhida);
            assertEquals(demandaAntigaPequena, escolhida);
            assertEquals(demandaAntigaPequena.getOrdemChegada(), escolhida.getOrdemChegada());

            // 2. falha
            assertNull(filaDoCaixa.selecionarDemanda(List.of(demandaConcluida), 1000.0));
        }
    }

    @Nested
    @DisplayName("Estratégia: Ferrari (Maior Demanda)")
    class FerrariTestes {

        @Test
        @DisplayName("Deve selecionar a maior demanda ignorando orçamento e retornar null se vazio")
        void deveSelecionarMaiorDemanda() {
            Ferrari ferrari = new Ferrari();
            List<Demanda> demandas = List.of(demandaAntigaPequena, demandaNovaGrande);

            // 1. sucesso
            Demanda escolhida = ferrari.selecionarDemanda(demandas, 0.0);
            assertNotNull(escolhida);
            assertEquals(demandaNovaGrande, escolhida);
            assertEquals(50, escolhida.getQuantidade());

            // 2. falha (sem demandas elegíveis)
            assertNull(ferrari.selecionarDemanda(List.of(demandaConcluida), 500.0));
        }
    }

    @Nested
    @DisplayName("Estratégia: Joãozinho (Máximo de Produtos no Orçamento)")
    class JoaozinhoTestes {

        @Test
        @DisplayName("Deve selecionar a maior viável no orçamento e retornar null caso contrário")
        void deveSelecionarMaiorDemandaViavel() {
            Joaozinho joaozinho = new Joaozinho();
            demandaAntigaPequena.atualizarCustoEstimado(2.0); // 5 * 2.0 = R$ 10.00
            demandaNovaGrande.atualizarCustoEstimado(2.0);    // 50 * 2.0 = R$ 100.00

            List<Demanda> demandas = List.of(demandaAntigaPequena, demandaNovaGrande);

            // 1. sucesso (verba alta - escolhe a de R$ 100.00)
            Demanda escolhidaVerbaAlta = joaozinho.selecionarDemanda(demandas, 150.0);
            assertEquals(demandaNovaGrande, escolhidaVerbaAlta);

            // 1. sucesso (verba média - escolhe a de R$ 10.00 pois a grande não cabe)
            Demanda escolhidaVerbaMedia = joaozinho.selecionarDemanda(demandas, 50.0);
            assertEquals(demandaAntigaPequena, escolhidaVerbaMedia);

            // 2. falha (verba insuficiente pra qualquer uma)
            Demanda escolhida = joaozinho.selecionarDemanda(demandas, 5.0);
            assertNull(escolhida);
        }
    }

    @Nested
    @DisplayName("Catálogo e Busca de Estratégias (CatalogoEstrategias)")
    class CatalogoEstrategiasTestes {

        @Test
        @DisplayName("Deve listar estratégias, validar padrão e buscar por nome")
        void deveListarEBuscarEstrategias() {
            // 1. sucesso
            List<EstrategiaProducao> todas = CatalogoEstrategias.todas();
            assertEquals(3, todas.size());
            assertEquals("Fila do Caixa", CatalogoEstrategias.padrao().getNomeEstrategia());

            assertTrue(CatalogoEstrategias.buscarPorNome("Fila do Caixa").isPresent());
            assertTrue(CatalogoEstrategias.buscarPorNome("ferrari").isPresent());
            assertTrue(CatalogoEstrategias.buscarPorNome("JOÃOZINHO").isPresent());

            // 2. falha (nome inexistente)
            assertTrue(CatalogoEstrategias.buscarPorNome("Inexistente").isEmpty());
        }
    }
}