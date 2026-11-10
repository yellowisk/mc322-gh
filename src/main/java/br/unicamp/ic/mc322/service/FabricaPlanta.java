package br.unicamp.ic.mc322.service;

import br.unicamp.ic.mc322.model.conveyor.Esteira;
import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.model.machine.MaquinaEmpacotamento;
import br.unicamp.ic.mc322.model.machine.MaquinaInspecao;
import br.unicamp.ic.mc322.model.machine.MaquinaProcessamento;
import br.unicamp.ic.mc322.model.product.CopoDeVidro;
import br.unicamp.ic.mc322.model.product.KitCopoDeVidro;
import br.unicamp.ic.mc322.model.product.PoteDeVidro;
import br.unicamp.ic.mc322.model.product.Produto;
import br.unicamp.ic.mc322.model.productionline.LinhaDeProducao;
import br.unicamp.ic.mc322.model.rawmaterial.MateriaPrima;
import br.unicamp.ic.mc322.model.report.Relatorio;
import br.unicamp.ic.mc322.model.scenario.ConfiguracaoCenario;
import br.unicamp.ic.mc322.strategy.CatalogoEstrategias;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;

import java.util.List;
import java.util.Optional;

/**
 * Monta fábricas: a fábrica nova de um cenário e a fábrica reconstruída a partir de dados salvos.
 * O catálogo de produtos e a esteira são fixos no código, então ficam aqui e não são salvos.<br>
 * Nenhum método daqui mexe em estado static: isso é com {@link Planta#ativar()}
 *
 * @author Heitor Almeida
 */
public final class FabricaPlanta {
    /** Nome da única linha de produção */
    public static final String NOME_LINHA = "Linha de Produção Ideal";

    /**
     * Modelos do catálogo, na ordem do menu. Compartilhados por todas as fábricas
     */
    private static final List<Produto> CATALOGO = List.of(
            new CopoDeVidro("Copo", 0.5),
            new PoteDeVidro("Pote", 1.2),
            new KitCopoDeVidro("Kit Copo", 3)
    );

    /** Só tem métodos static, não é p/ instanciar */
    private FabricaPlanta() {
    }

    /**
     * Fábrica nova do cenário: estoque inicial de vidro, verba do cenário, as 3 máquinas novas e uma
     * demanda zerada por produto do catálogo
     *
     * @param cenario cenário escolhido
     * @return fábrica pronta, ainda não ativada
     */
    public static Planta criarPadrao(ConfiguracaoCenario cenario) {
        LinhaDeProducao linha = new LinhaDeProducao(NOME_LINHA);
        linha.adicionarMaquina(new MaquinaProcessamento(
                "Máquina de Processamento", 20, 0.25,
                0.78, cenario.getMultiplicadorCenario(),
                cenario.getDesgasteMaximo(), 2, 3
        ));
        linha.adicionarMaquina(new MaquinaEmpacotamento(
                "Máquina de Empacotamento", 20, 0.15,
                0.5, cenario.getMultiplicadorCenario(),
                cenario.getDesgasteMaximo(), 2 // 2 sensores
        ));
        linha.adicionarMaquina(new MaquinaInspecao(
                "Máquina de Inspeção", 20, 0.10,
                0.43, cenario.getMultiplicadorCenario(),
                cenario.getDesgasteMaximo(), 2, 3
        ));

        GerenciadorProducao gerenciador = criarGerenciador(
                new MateriaPrima("Vidro", 50, "kg", 5, 1), cenario.getVerba(), linha);
        CATALOGO.forEach(p -> gerenciador.registrarDemanda(p, 0));
        gerenciador.setEstrategiaAtual(CatalogoEstrategias.padrao());
        return new Planta(cenario, gerenciador, linha);
    }

    /**
     * Fábrica reconstruída a partir de dados salvos. O catálogo e a esteira vêm do código; o resto vem
     * dos parâmetros
     *
     * @param cenario cenário salvo
     * @param materiaPrima estoque salvo
     * @param verba verba salva, em R$
     * @param linha linha c/ as máquinas já reconstruídas
     * @param demandas uma demanda por produto do catálogo
     * @param estrategia estratégia salva
     * @return fábrica pronta, ainda não ativada
     * @throws IllegalArgumentException se a verba for negativa ou as demandas não baterem c/ o catálogo
     */
    public static Planta montar(ConfiguracaoCenario cenario, MateriaPrima materiaPrima, float verba,
                                LinhaDeProducao linha, List<Demanda> demandas,
                                EstrategiaProducao estrategia) {
        if (verba < 0) {
            throw new IllegalArgumentException("Dessa vez não é! A verba não pode ser negativa: R$ " + verba + ".");
        }
        GerenciadorProducao gerenciador = criarGerenciador(materiaPrima, verba, linha);
        gerenciador.restaurarDemandas(demandas);
        gerenciador.setEstrategiaAtual(estrategia);
        return new Planta(cenario, gerenciador, linha);
    }

    /**
     * Modelos do catálogo, na ordem do menu
     *
     * @return Copo, Pote e Kit Copo (lista imutável)
     */
    public static List<Produto> getCatalogo() {
        return CATALOGO;
    }

    /**
     * Procura um modelo do catálogo pelo nome, ex.: p/ reconstruir as demandas salvas num arquivo
     *
     * @param nome nome do produto, ex.: "Kit Copo"
     * @return o modelo, ou vazio se nenhum produto tiver esse nome
     */
    public static Optional<Produto> buscarNoCatalogo(String nome) {
        return CATALOGO.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    /**
     * cria gerenciador c/ o catálogo (ainda sem demandas) e a esteira
     *
     * @param materiaPrima estoque de vidro
     * @param verba verba, em R$
     * @param linha linha de produção
     * @return gerenciador ainda sem estratégia
     */
    private static GerenciadorProducao criarGerenciador(MateriaPrima materiaPrima, float verba, LinhaDeProducao linha) {
        GerenciadorProducao gerenciador = new GerenciadorProducao(materiaPrima, verba, new Relatorio(), linha);
        CATALOGO.forEach(gerenciador::adicionarAoCatalogo);
        gerenciador.adicionarEsteira(new Esteira("Esteira Ligeira", 20));
        return gerenciador;
    }
}
