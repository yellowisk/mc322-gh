package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.model.machine.Maquina;
import br.unicamp.ic.mc322.model.machine.MaquinaEmpacotamento;
import br.unicamp.ic.mc322.model.machine.MaquinaInspecao;
import br.unicamp.ic.mc322.model.machine.MaquinaProcessamento;
import br.unicamp.ic.mc322.model.productionline.EtapaProducao;
import br.unicamp.ic.mc322.model.productionline.LinhaDeProducao;
import br.unicamp.ic.mc322.model.rawmaterial.MateriaPrima;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.service.Planta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


/**
 * Transforma uma {@link Planta} na árvore XML do save para que {@link PersistenciaPlanta} grave no arquivo
 *
 * @author Heitor Almeida
 */
class PlantaXmlEscritor {
    /** Nome temático da fábrica */
    private static final String NOME_FABRICA = "Fábrica Ideal";

    private Document documento;

    /**
     * Monta o XML c/ tudo que é salvo da fábrica: cenário, verba, estratégia, matéria-prima, máquinas c/ as
     * partes e demandas
     *
     * @param planta fábrica a salvar
     * @return documento pronto p/ gravar
     */
    Document escrever(Planta planta) {
        this.documento = PlantaXml.novoParser().newDocument();
        this.documento.setXmlStandalone(true);
        GerenciadorProducao gerenciador = planta.getGerenciador();

        Element raiz = elemento("planta");
        raiz.setAttribute("nome", NOME_FABRICA);
        raiz.setAttribute("versao", PlantaXml.VERSAO);
        raiz.setAttribute("salvoEm", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
        this.documento.appendChild(raiz);

        raiz.appendChild(elemento("cenario", "tipo", planta.getCenario().getTipo()));
        raiz.appendChild(elemento("financeiro", "verba", Float.toString(gerenciador.getVerba())));
        raiz.appendChild(elemento("estrategia", "nome", gerenciador.getEstrategiaAtual().getNomeEstrategia()));

        Element materiasPrimas = elemento("materiasPrimas");
        materiasPrimas.appendChild(escreverMateriaPrima(gerenciador.getMateriaPrima()));
        raiz.appendChild(materiasPrimas);

        raiz.appendChild(escreverLinha(planta.getLinha()));

        Element demandas = elemento("demandas");
        gerenciador.getDemandas().forEach(d -> demandas.appendChild(escreverDemanda(d)));
        raiz.appendChild(demandas);

        return this.documento;
    }

    /**
     * {@code <materiaPrima nome unidade>} c/ estoque, mínimo e preço
     *
     * @param materiaPrima matéria-prima a salvar
     * @return elemento da matéria-prima
     */
    private Element escreverMateriaPrima(MateriaPrima materiaPrima) {
        Element el = elemento("materiaPrima");
        el.setAttribute("nome", materiaPrima.getNome());
        el.setAttribute("unidade", materiaPrima.getUnidade());
        el.appendChild(elementoComTexto("quantidade", Double.toString(materiaPrima.getQuantidade())));
        el.appendChild(elementoComTexto("quantidadeMinima", Integer.toString(materiaPrima.getQuantidadeMinima())));
        el.appendChild(elementoComTexto("preco", Float.toString(materiaPrima.getPreco())));
        return el;
    }

    /**
     * {@code <linhaDeProducao>} c/ as máquinas na ordem das etapas
     *
     * @param linha linha a salvar
     * @return elemento da linha
     */
    private Element escreverLinha(LinhaDeProducao linha) {
        Element el = elemento("linhaDeProducao", "nome", linha.getNome());
        linha.getMaquinas().forEach(m -> el.appendChild(escreverMaquina(m)));
        return el;
    }

    /**
     * {@code <maquina tipo>} c/ nome, configuração, motor, sensores e a parte específica do subtipo.
     * A quantidade de sensores/resistências/câmeras é a quantidade de elementos, não um atributo à parte
     *
     * @param maquina máquina a salvar
     * @return elemento da máquina
     */
    private Element escreverMaquina(Maquina maquina) {
        Element el = elemento("maquina");
        el.appendChild(elementoComTexto("nome", maquina.getNome()));

        Element configuracao = elemento("configuracao");
        configuracao.setAttribute("capacidadeMaxima", Integer.toString(maquina.getCapacidadeMaxima()));
        configuracao.setAttribute("chanceFalhaBase", Double.toString(maquina.getChanceFalhaBase()));
        configuracao.setAttribute("custoOperacao", Double.toString(maquina.getCustoOperacao()));
        el.appendChild(configuracao);

        el.appendChild(elemento("motor", "saude", Integer.toString(maquina.getSaude())));
        el.appendChild(lista("sensores", "sensor", "calibracao", maquina.getCalibracoesSensores()));

        if (maquina instanceof MaquinaProcessamento p) {
            el.setAttribute("tipo", EtapaProducao.PROCESSAMENTO.name());
            el.appendChild(lista("resistencias", "resistencia", "queimada", p.getResistenciasQueimadas()));
        } else if (maquina instanceof MaquinaEmpacotamento e) {
            el.setAttribute("tipo", EtapaProducao.EMPACOTAMENTO.name());
            el.appendChild(elemento("seladora", "desgasteLamina", Double.toString(e.getDesgasteLamina())));
        } else if (maquina instanceof MaquinaInspecao i) {
            el.setAttribute("tipo", EtapaProducao.INSPECAO.name());
            el.appendChild(lista("cameras", "camera", "lenteSuja", i.getLentesSujas()));
        } else {
            throw new IllegalStateException("Tipo de máquina sem formato de save: " + maquina.getClass().getSimpleName());
        }
        return el;
    }

    /**
     * {@code <demanda produto ordemChegada>} c/ quantidade, progresso, status e tempo
     *
     * @param demanda demanda a salvar
     * @return elemento da demanda
     */
    private Element escreverDemanda(Demanda demanda) {
        Element el = elemento("demanda");
        el.setAttribute("produto", demanda.getNomeProduto());
        el.setAttribute("ordemChegada", Long.toString(demanda.getOrdemChegada()));
        el.appendChild(elementoComTexto("quantidade", Integer.toString(demanda.getQuantidade())));
        el.appendChild(elementoComTexto("quantidadeProduzida", Integer.toString(demanda.getQuantidadeProduzida())));
        el.appendChild(elementoComTexto("status", demanda.getStatus().name()));
        el.appendChild(elementoComTexto("tempoTotalProducao", Double.toString(demanda.getTempoTotalProducao())));
        return el;
    }

    // === Auxiliares ===

    /**
     * Lista de partes iguais, ex.: {@code <sensores><sensor id="1" calibracao="1.0"/>...</sensores>}.
     * Os ids começam em 1, na ordem da lista
     *
     * @param tagLista tag que agrupa, ex.: "sensores"
     * @param tagItem tag de cada parte, ex.: "sensor"
     * @param atributo atributo c/ o estado de cada parte
     * @param valores estado de cada parte, na ordem
     * @return elemento da lista
     */
    private Element lista(String tagLista, String tagItem, String atributo, List<?> valores) {
        Element lista = elemento(tagLista);
        for (int i = 0; i < valores.size(); i++) {
            Element item = elemento(tagItem, "id", Integer.toString(i + 1));
            item.setAttribute(atributo, valores.get(i).toString());
            lista.appendChild(item);
        }
        return lista;
    }

    private Element elemento(String tag) {
        return this.documento.createElement(tag);
    }

    private Element elemento(String tag, String atributo, String valor) {
        Element el = elemento(tag);
        el.setAttribute(atributo, valor);
        return el;
    }

    private Element elementoComTexto(String tag, String texto) {
        Element el = elemento(tag);
        el.setTextContent(texto);
        return el;
    }
}
