package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.ArquivoPersistenciaCorrompidoException;
import br.unicamp.ic.mc322.exception.ArquivoPersistenciaInexistenteException;
import br.unicamp.ic.mc322.exception.ArquivoPersistenciaVazioException;
import br.unicamp.ic.mc322.exception.DadosPersistidosInvalidosException;
import br.unicamp.ic.mc322.exception.FalhaEscritaPersistenciaException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.model.product.CopoDeVidro;
import br.unicamp.ic.mc322.service.Planta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Situações de erro ao salvar e carregar. Os arquivos quebrados são feitos salvando uma fábrica válida e
 * estragando um pedaço do XML, assim eles acompanham o formato de verdade
 */
@DisplayName("PersistenciaPlanta: Erros")
class PersistenciaPlantaErrosTest {
    @TempDir
    Path pasta;

    private Path arquivo;
    private PersistenciaPlanta persistencia;

    @BeforeEach
    void apontarParaPastaTemporaria() {
        arquivo = pasta.resolve("planta.xml");
        persistencia = new PersistenciaPlanta(arquivo);
    }

    /** Salva a fábrica alterada e troca a primeira ocorrência de {@code regex} no arquivo */
    private void salvarEEstragar(String regex, String substituto) throws Exception {
        persistencia.salvar(PlantasDeTeste.alterada());
        String xml = Files.readString(arquivo);
        String estragado = xml.replaceFirst(regex, substituto);
        assertTrue(!estragado.equals(xml), "o teste não estragou nada: \"" + regex + "\" não está no XML");
        Files.writeString(arquivo, estragado);
    }

    private <T extends PersistenciaException> T carregarEsperando(Class<T> tipo, String trechoDaMensagem) {
        T erro = assertThrows(tipo, persistencia::carregar);
        assertTrue(erro.getMessage().contains(trechoDaMensagem),
                "a mensagem deveria citar \"" + trechoDaMensagem + "\": " + erro.getMessage());
        return erro;
    }

    // === Arquivo ===

    @Test
    @DisplayName("Arquivo que não existe lança ArquivoPersistenciaInexistenteException")
    void arquivoInexistente() {
        carregarEsperando(ArquivoPersistenciaInexistenteException.class, "Nenhum save");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   \n\t  "})
    @DisplayName("Arquivo vazio ou só c/ espaços lança ArquivoPersistenciaVazioException")
    void arquivoVazio(String conteudo) throws Exception {
        Files.writeString(arquivo, conteudo);

        carregarEsperando(ArquivoPersistenciaVazioException.class, "vazio");
    }

    @Test
    @DisplayName("Arquivo que não é XML lança ArquivoPersistenciaCorrompidoException")
    void arquivoQueNaoEhXml() throws Exception {
        Files.writeString(arquivo, "isso aqui não é um save");

        carregarEsperando(ArquivoPersistenciaCorrompidoException.class, "não é um XML válido");
    }

    @Test
    @DisplayName("Arquivo cortado no meio lança ArquivoPersistenciaCorrompidoException")
    void arquivoCortado() throws Exception {
        persistencia.salvar(PlantasDeTeste.alterada());
        String xml = Files.readString(arquivo);
        Files.writeString(arquivo, xml.substring(0, xml.length() / 2));

        carregarEsperando(ArquivoPersistenciaCorrompidoException.class, "não é um XML válido");
    }

    // === Estrutura ===

    @Test
    @DisplayName("Raiz diferente de <planta> lança ArquivoPersistenciaCorrompidoException")
    void raizErrada() throws Exception {
        Files.writeString(arquivo, "<?xml version=\"1.0\"?>\n<fabrica versao=\"1\"/>");

        carregarEsperando(ArquivoPersistenciaCorrompidoException.class, "<fabrica>");
    }

    @Test
    @DisplayName("Versão desconhecida do formato lança ArquivoPersistenciaCorrompidoException")
    void versaoDesconhecida() throws Exception {
        salvarEEstragar("versao=\"1\"", "versao=\"99\"");

        carregarEsperando(ArquivoPersistenciaCorrompidoException.class, "versão 99");
    }

    @Test
    @DisplayName("Faltando a linha de produção lança ArquivoPersistenciaCorrompidoException")
    void faltandoLinha() throws Exception {
        salvarEEstragar("(?s)<linhaDeProducao.*</linhaDeProducao>", "");

        carregarEsperando(ArquivoPersistenciaCorrompidoException.class, "<linhaDeProducao>");
    }

    @Test
    @DisplayName("Faltando um atributo lança ArquivoPersistenciaCorrompidoException")
    void faltandoAtributo() throws Exception {
        salvarEEstragar("<motor saude=\"72\"/>", "<motor/>");

        carregarEsperando(ArquivoPersistenciaCorrompidoException.class, "saude");
    }

    @Test
    @DisplayName("Tag repetida onde só pode ter uma lança ArquivoPersistenciaCorrompidoException")
    void tagRepetida() throws Exception {
        salvarEEstragar("<motor saude=\"72\"/>", "<motor saude=\"72\"/><motor saude=\"50\"/>");

        carregarEsperando(ArquivoPersistenciaCorrompidoException.class, "mais de uma vez");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "", "7.5"})
    @DisplayName("Número inteiro inválido lança ArquivoPersistenciaCorrompidoException")
    void inteiroInvalido(String valor) throws Exception {
        salvarEEstragar("saude=\"72\"", "saude=\"" + valor + "\"");

        carregarEsperando(ArquivoPersistenciaCorrompidoException.class, "saude");
    }

    @Test
    @DisplayName("Verba que não é um número lança ArquivoPersistenciaCorrompidoException")
    void verbaNaoNumerica() throws Exception {
        salvarEEstragar("verba=\"[^\"]*\"", "verba=\"muito\"");

        carregarEsperando(ArquivoPersistenciaCorrompidoException.class, "verba");
    }

    @Test
    @DisplayName("Booleano diferente de true/false lança ArquivoPersistenciaCorrompidoException")
    void booleanoInvalido() throws Exception {
        salvarEEstragar("queimada=\"true\"", "queimada=\"talvez\"");

        carregarEsperando(ArquivoPersistenciaCorrompidoException.class, "true ou false");
    }

    // === Regras da fábrica ===

    @Test
    @DisplayName("Saúde do motor acima de 100 lança DadosPersistidosInvalidosException")
    void saudeAcimaDoMaximo() throws Exception {
        salvarEEstragar("saude=\"72\"", "saude=\"150\"");

        DadosPersistidosInvalidosException e = carregarEsperando(DadosPersistidosInvalidosException.class, "processamento");
        assertInstanceOf(IllegalArgumentException.class, e.getCause(), "a causa é a recusa do domínio");
    }

    @Test
    @DisplayName("Calibração de sensor acima de 1 lança DadosPersistidosInvalidosException")
    void calibracaoAcimaDoMaximo() throws Exception {
        salvarEEstragar("calibracao=\"0.55\"", "calibracao=\"1.7\"");

        carregarEsperando(DadosPersistidosInvalidosException.class, "calibração");
    }

    @Test
    @DisplayName("Máquina sem sensores lança DadosPersistidosInvalidosException (o construtor exige pelo menos um)")
    void maquinaSemSensores() throws Exception {
        salvarEEstragar("(?s)<sensores>.*?</sensores>", "<sensores/>");

        carregarEsperando(DadosPersistidosInvalidosException.class, "sensor");
    }

    @Test
    @DisplayName("Id de sensor fora de ordem lança DadosPersistidosInvalidosException")
    void idDeSensorForaDeOrdem() throws Exception {
        salvarEEstragar("calibracao=\"0.55\" id=\"2\"", "calibracao=\"0.55\" id=\"5\"");

        carregarEsperando(DadosPersistidosInvalidosException.class, "id 5");
    }

    @Test
    @DisplayName("Máquinas fora da ordem das etapas lançam DadosPersistidosInvalidosException")
    void maquinaForaDeOrdem() throws Exception {
        salvarEEstragar("tipo=\"PROCESSAMENTO\"", "tipo=\"INSPECAO\"");

        carregarEsperando(DadosPersistidosInvalidosException.class, "PROCESSAMENTO");
    }

    @Test
    @DisplayName("Linha c/ máquina faltando lança DadosPersistidosInvalidosException")
    void maquinaFaltando() throws Exception {
        salvarEEstragar("(?s)<maquina tipo=\"INSPECAO\">.*?</maquina>", "");

        carregarEsperando(DadosPersistidosInvalidosException.class, "3 máquinas");
    }

    @Test
    @DisplayName("Demanda salva Em Produção lança DadosPersistidosInvalidosException")
    void demandaEmProducao() throws Exception {
        salvarEEstragar("<status>CANCELADA</status>", "<status>EM_PRODUCAO</status>");

        carregarEsperando(DadosPersistidosInvalidosException.class, "demanda de Copo");
    }

    @Test
    @DisplayName("Status de demanda que não existe lança DadosPersistidosInvalidosException")
    void statusInexistente() throws Exception {
        salvarEEstragar("<status>CANCELADA</status>", "<status>ATRASADA</status>");

        carregarEsperando(DadosPersistidosInvalidosException.class, "demanda de Copo");
    }

    @Test
    @DisplayName("Mais unidades produzidas que pedidas lança DadosPersistidosInvalidosException")
    void produzidaMaiorQueQuantidade() throws Exception {
        salvarEEstragar("<quantidadeProduzida>12</quantidadeProduzida>", "<quantidadeProduzida>40</quantidadeProduzida>");

        carregarEsperando(DadosPersistidosInvalidosException.class, "40 de 30");
    }

    @Test
    @DisplayName("Cenário que não existe lança DadosPersistidosInvalidosException")
    void cenarioInexistente() throws Exception {
        salvarEEstragar("tipo=\"Apocalíptico\"", "tipo=\"Infernal\"");

        carregarEsperando(DadosPersistidosInvalidosException.class, "Infernal");
    }

    @Test
    @DisplayName("Estratégia que não existe lança DadosPersistidosInvalidosException")
    void estrategiaInexistente() throws Exception {
        salvarEEstragar("nome=\"Ferrari\"", "nome=\"Eitcha!\"");

        carregarEsperando(DadosPersistidosInvalidosException.class, "Eitcha!");
    }

    @Test
    @DisplayName("Produto fora do catálogo lança DadosPersistidosInvalidosException")
    void produtoInexistente() throws Exception {
        salvarEEstragar("produto=\"Pote\"", "produto=\"Taça\"");

        carregarEsperando(DadosPersistidosInvalidosException.class, "Taça");
    }

    @Test
    @DisplayName("Duas demandas do mesmo produto lançam DadosPersistidosInvalidosException")
    void demandaRepetida() throws Exception {
        salvarEEstragar("produto=\"Pote\"", "produto=\"Copo\"");

        carregarEsperando(DadosPersistidosInvalidosException.class, "Copo");
    }

    @Test
    @DisplayName("Verba negativa lança DadosPersistidosInvalidosException")
    void verbaNegativa() throws Exception {
        salvarEEstragar("verba=\"[^\"]*\"", "verba=\"-10.0\"");

        carregarEsperando(DadosPersistidosInvalidosException.class, "verba");
    }

    @Test
    @DisplayName("Estoque negativo lança DadosPersistidosInvalidosException")
    void estoqueNegativo() throws Exception {
        salvarEEstragar("<quantidade>37.5</quantidade>", "<quantidade>-1</quantidade>");

        carregarEsperando(DadosPersistidosInvalidosException.class, "matéria-prima");
    }

    @Test
    @DisplayName("Todas as exceções de persistência são PlantaException, então o menu principal também as trata")
    void excecoesSaoDaHierarquiaDaFabrica() {
        PersistenciaException e = assertThrows(PersistenciaException.class, persistencia::carregar);

        assertInstanceOf(PlantaException.class, e);
    }

    // === Escrita ===

    @Test
    @DisplayName("Salvar num caminho que é uma pasta lança FalhaEscritaPersistenciaException")
    void salvarEmPasta() throws Exception {
        Files.createDirectory(arquivo);

        assertThrows(FalhaEscritaPersistenciaException.class, () -> persistencia.salvar(PlantasDeTeste.padrao()));
    }

    @Test
    @DisplayName("Falha ao salvar não estraga o save anterior")
    void falhaAoSalvarPreservaSaveAnterior() throws Exception {
        persistencia.salvar(PlantasDeTeste.alterada());
        String anterior = Files.readString(arquivo);
        Files.setPosixFilePermissions(pasta, PosixFilePermissions.fromString("r-xr-xr-x"));
        try {
            assumeFalse(Files.isWritable(pasta), "Ignoramos o teste se ele for rodado usando root, pois não será" +
                    "possível fazer a gravação falhar");

            assertThrows(FalhaEscritaPersistenciaException.class, () -> persistencia.salvar(PlantasDeTeste.padrao()));

            assertEquals(anterior, Files.readString(arquivo));
            assertEquals("Apocalíptico", persistencia.carregar().getCenario().getTipo());
        } finally {
            Files.setPosixFilePermissions(pasta, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
    }

    // === Fábrica atual preservada ===

    @Test
    @DisplayName("Carregamento que falha não muda as máquinas da fábrica em uso, mesmo de outro cenário")
    void carregamentoFalhoNaoMudaFabricaAtual() throws Exception {
        Planta atual = PlantasDeTeste.padrao(); // Ideal, multiplicador 0.5
        double multiplicadorAntes = atual.getLinha().getMaquina(0).getMultiplicadorCenario();
        salvarEEstragar("produto=\"Pote\"", "produto=\"Copo\"");

        assertThrows(DadosPersistidosInvalidosException.class, persistencia::carregar);

        assertEquals(multiplicadorAntes, atual.getLinha().getMaquina(0).getMultiplicadorCenario());
        assertEquals(3, atual.getLinha().getMaquinas().size());
    }

    @Test
    @DisplayName("Carregamento que falha não mexe no contador de chegada das demandas")
    void carregamentoFalhoNaoMudaContadores() throws Exception {
        salvarEEstragar("produto=\"Pote\"", "produto=\"Copo\"");
        long antes = new Demanda(new CopoDeVidro("Copo", 0.5), 1).getOrdemChegada();

        assertThrows(DadosPersistidosInvalidosException.class, persistencia::carregar);
        long depois = new Demanda(new CopoDeVidro("Copo", 0.5), 1).getOrdemChegada();

        assertEquals(antes + 1, depois);
    }
}
