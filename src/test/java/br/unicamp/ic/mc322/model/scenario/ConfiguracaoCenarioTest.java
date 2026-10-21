package br.unicamp.ic.mc322.model.scenario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Modelo p/ os outros testes do projeto:
 */
@DisplayName("ConfiguracaoCenario")
class ConfiguracaoCenarioTest {

    @Test
    @DisplayName("Existem os três cenários do enunciado, na ordem do seletor")
    void cenariosNaOrdemDoSeletor() {
        assertEquals(3, ConfiguracaoCenario.CENARIOS.size());
        assertEquals("Ideal", ConfiguracaoCenario.CENARIOS.get(0).getTipo());
        assertEquals("Intermediário", ConfiguracaoCenario.CENARIOS.get(1).getTipo());
        assertEquals("Apocalíptico", ConfiguracaoCenario.CENARIOS.get(2).getTipo());
    }

    @Test
    @DisplayName("Quanto pior o cenário, menor a verba e maiores o multiplicador de falha e o desgaste")
    void cenariosPioresSaoMaisDificeis() {
        ConfiguracaoCenario ideal = ConfiguracaoCenario.CENARIOS.get(0);
        ConfiguracaoCenario intermediario = ConfiguracaoCenario.CENARIOS.get(1);
        ConfiguracaoCenario apocaliptico = ConfiguracaoCenario.CENARIOS.get(2);

        assertTrue(ideal.getVerba() > intermediario.getVerba());
        assertTrue(intermediario.getVerba() > apocaliptico.getVerba());
        assertTrue(ideal.getMultiplicadorCenario() < intermediario.getMultiplicadorCenario());
        assertTrue(intermediario.getMultiplicadorCenario() < apocaliptico.getMultiplicadorCenario());
        assertTrue(ideal.getDesgasteMaximo() < apocaliptico.getDesgasteMaximo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ideal", "Intermediário", "Apocalíptico"})
    @DisplayName("Buscar pelo tipo acha cada um dos cenários")
    void buscarPorTipoAchaCadaCenario(String tipo) {
        Optional<ConfiguracaoCenario> encontrado = ConfiguracaoCenario.buscarPorTipo(tipo);

        assertTrue(encontrado.isPresent(), "não achou o cenário " + tipo);
        assertEquals(tipo, encontrado.get().getTipo());
    }

    @Test
    @DisplayName("Buscar pelo tipo não diferencia maiúsculas de minúsculas")
    void buscarPorTipoIgnoraMaiusculas() {
        Optional<ConfiguracaoCenario> encontrado = ConfiguracaoCenario.buscarPorTipo("aPoCaLíPtIcO");

        assertEquals("Apocalíptico", encontrado.orElseThrow().getTipo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Infernal", ""})
    @DisplayName("Buscar por um tipo que não existe devolve vazio")
    void buscarPorTipoInexistenteDevolveVazio(String tipo) {
        assertTrue(ConfiguracaoCenario.buscarPorTipo(tipo).isEmpty());
    }

    @Test
    @DisplayName("A lista de cenários não pode ser alterada")
    void listaDeCenariosEhImutavel() {
        ConfiguracaoCenario extra = new ConfiguracaoCenario("Teste", "Teste", 1, 1, 1);

        assertThrows(UnsupportedOperationException.class, () -> ConfiguracaoCenario.CENARIOS.add(extra));
    }

    @Test
    @DisplayName("O nome de exibição junta o nome temático e o tipo")
    void nomeDeExibicaoJuntaNomeETipo() {
        ConfiguracaoCenario cenario = new ConfiguracaoCenario("Mercadinho Teste", "Teste", 100, 1, 1);

        assertEquals("Mercadinho Teste (Teste)", cenario.getNomeExibicao());
    }
}
