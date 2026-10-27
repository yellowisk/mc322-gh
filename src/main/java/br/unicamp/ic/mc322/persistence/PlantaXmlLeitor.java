package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.ArquivoPersistenciaCorrompidoException;
import br.unicamp.ic.mc322.exception.DadosPersistidosInvalidosException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.model.demand.StatusDemanda;
import br.unicamp.ic.mc322.model.machine.Maquina;
import br.unicamp.ic.mc322.model.machine.MaquinaEmpacotamento;
import br.unicamp.ic.mc322.model.machine.MaquinaInspecao;
import br.unicamp.ic.mc322.model.machine.MaquinaProcessamento;
import br.unicamp.ic.mc322.model.product.Produto;
import br.unicamp.ic.mc322.model.productionline.EtapaProducao;
import br.unicamp.ic.mc322.model.productionline.LinhaDeProducao;
import br.unicamp.ic.mc322.model.rawmaterial.MateriaPrima;
import br.unicamp.ic.mc322.model.scenario.ConfiguracaoCenario;
import br.unicamp.ic.mc322.service.FabricaPlanta;
import br.unicamp.ic.mc322.service.Planta;
import br.unicamp.ic.mc322.strategy.CatalogoEstrategias;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


/**
 * Reconstrói uma {@link Planta} nova a partir da árvore XML do save, validando tudo antes:
 * <ul>
 *     <li>estrutura (ArquivoPersistenciaCorrompidoException});</li>
 *     <li>regras da fábrica {@link DadosPersistidosInvalidosException};</li>
 * </ul>
 * As regras não são repetidas aqui, o domínio as cumpre. O leitor só acrescenta onde o problema está no arquivo.
 * Nada aqui mexe numa fábrica existente nem em estado static
 *
 * @author Heitor Almeida
 */
class PlantaXmlLeitor {

    /**
     * Lê o documento inteiro e monta a fábrica
     *
     * @param documento XML já lido do arquivo
     * @return fábrica nova, ainda não ativada
     * @throws ArquivoPersistenciaCorrompidoException se a estrutura não for a esperada
     * @throws DadosPersistidosInvalidosException se algum valor quebrar uma regra da fábrica
     */
    Planta ler(Document documento) {
        Element raiz = documento.getDocumentElement();
        if (!"planta".equals(raiz.getTagName())) {
            throw corrompido("a raiz do arquivo deveria ser <" + "planta" + ">, mas é <" + raiz.getTagName() + ">");
        }
        String versao = atributo(raiz, "versao");
        if (!PlantaXml.VERSAO.equals(versao)) {
            throw corrompido("versão " + versao + " do arquivo não é suportada (esperada: " + PlantaXml.VERSAO + ")");
        }

        String tipoCenario = atributo(filho(raiz, "cenario"), "tipo");
        ConfiguracaoCenario cenario = ConfiguracaoCenario.buscarPorTipo(tipoCenario)
                .orElseThrow(() -> invalido("cenário \"" + tipoCenario + "\" não existe"));

        float verba = (float) lerDouble(filho(raiz, "financeiro"), "verba");

        String nomeEstrategia = atributo(filho(raiz, "estrategia"), "nome");
        EstrategiaProducao estrategia = CatalogoEstrategias.buscarPorNome(nomeEstrategia)
                .orElseThrow(() -> invalido("estratégia \"" + nomeEstrategia + "\" não existe"));

        MateriaPrima materiaPrima = lerMateriaPrima(filho(raiz, "materiasPrimas"));
        LinhaDeProducao linha = lerLinha(filho(raiz, "linhaDeProducao"), cenario);
        List<Demanda> demandas = lerDemandas(filho(raiz, "demandas"));

        return validando("fábrica", () ->
                FabricaPlanta.montar(cenario, materiaPrima, verba, linha, demandas, estrategia));
    }

    /**
     * A fábrica usa uma matéria-prima só (o vidro), então a lista tem que ter exatamente uma
     *
     * @param materiasPrimas elemento {@code <materiasPrimas>}
     * @return matéria-prima reconstruída
     */
    private MateriaPrima lerMateriaPrima(Element materiasPrimas) {
        List<Element> lista = filhos(materiasPrimas, "materiaPrima");
        if (lista.size() != 1) {
            throw invalido("a fábrica tem uma matéria-prima, mas o arquivo tem " + lista.size());
        }
        Element el = lista.getFirst();
        String nome = atributo(el, "nome");
        String unidade = atributo(el, "unidade");
        double quantidade = lerDouble(filho(el, "quantidade"));
        int minima = lerInt(filho(el, "quantidadeMinima"));
        float preco = (float) lerDouble(filho(el, "preco"));
        return validando("matéria-prima " + nome, () -> new MateriaPrima(nome, quantidade, unidade, minima, preco));
    }

    /**
     * A linha tem uma máquina por etapa, na ordem das etapas
     *
     * @param el elemento {@code <linhaDeProducao>}
     * @param cenario cenário da fábrica
     * @return linha c/ as máquinas reconstruídas
     */
    private LinhaDeProducao lerLinha(Element el, ConfiguracaoCenario cenario) {
        List<Element> maquinas = filhos(el, "maquina");
        EtapaProducao[] etapas = EtapaProducao.values();
        if (maquinas.size() != etapas.length) {
            throw invalido("a linha precisa de " + etapas.length + " máquinas, mas o arquivo tem " + maquinas.size());
        }
        LinhaDeProducao linha = validando("linha", () -> new LinhaDeProducao(atributo(el, "nome")));
        for (int i = 0; i < etapas.length; i++) {
            linha.adicionarMaquina(lerMaquina(maquinas.get(i), etapas[i], cenario));
        }
        return linha;
    }

    /**
     * Cria a máquina pelo construtor normal c/ a quantidade
     * de partes do arquivo, e depois restaura o estado de cada parte
     *
     * @param el elemento {@code <maquina>}
     * @param etapa etapa que essa posição da linha tem que ter
     * @param cenario cenário da fábrica
     * @return máquina reconstruída
     */
    private Maquina lerMaquina(Element el, EtapaProducao etapa, ConfiguracaoCenario cenario) {
        String contexto = "máquina de " + etapa.getNome();
        String tipo = atributo(el, "tipo");
        if (!etapa.name().equals(tipo)) {
            throw invalido("a máquina da etapa " + (etapa.getCodigo() + 1) + " deveria ser do tipo " + etapa.name()
                    + ", mas é " + tipo);
        }

        String nome = lerTexto(filho(el, "nome"));
        Element configuracao = filho(el, "configuracao");
        int capacidade = lerInt(configuracao, "capacidadeMaxima");
        double chanceFalha = lerDouble(configuracao, "chanceFalhaBase");
        double custo = lerDouble(configuracao, "custoOperacao");
        int saude = lerInt(filho(el, "motor"), "saude");
        List<Double> calibracoes = new ArrayList<>();
        for (Element sensor : lerLista(filho(el, "sensores"), "sensor")) {
            calibracoes.add(lerDouble(sensor, "calibracao"));
        }
        double multiplicador = cenario.getMultiplicadorCenario();
        int desgaste = cenario.getDesgasteMaximo();

        Maquina maquina = switch (etapa) {
            case PROCESSAMENTO -> {
                List<Boolean> queimadas = lerBooleans(lerLista(filho(el, "resistencias"), "resistencia"), "queimada");
                yield validando(contexto, () -> {
                    MaquinaProcessamento m = new MaquinaProcessamento(nome, capacidade, chanceFalha, custo,
                            multiplicador, desgaste, calibracoes.size(), queimadas.size());
                    m.restaurarResistencias(queimadas);
                    return m;
                });
            }
            case EMPACOTAMENTO -> {
                double desgasteLamina = lerDouble(filho(el, "seladora"), "desgasteLamina");
                yield validando(contexto, () -> {
                    MaquinaEmpacotamento m = new MaquinaEmpacotamento(nome, capacidade, chanceFalha, custo,
                            multiplicador, desgaste, calibracoes.size());
                    m.restaurarSeladora(desgasteLamina);
                    return m;
                });
            }
            case INSPECAO -> {
                List<Boolean> lentesSujas = lerBooleans(lerLista(filho(el, "cameras"), "camera"), "lenteSuja");
                yield validando(contexto, () -> {
                    MaquinaInspecao m = new MaquinaInspecao(nome, capacidade, chanceFalha, custo,
                            multiplicador, desgaste, calibracoes.size(), lentesSujas.size());
                    m.restaurarCameras(lentesSujas);
                    return m;
                });
            }
        };
        validando(contexto, () -> {
            maquina.restaurarEstado(saude, calibracoes);
            return maquina;
        });
        return maquina;
    }

    /**
     * Uma demanda por produto do catálogo
     *
     * @param el elemento {@code <demandas>}
     * @return demandas reconstruídas, na ordem do arquivo
     */
    private List<Demanda> lerDemandas(Element el) {
        List<Demanda> demandas = new ArrayList<>();
        for (Element d : filhos(el, "demanda")) {
            String nomeProduto = atributo(d, "produto");
            String contexto = "demanda de " + nomeProduto;
            Produto modelo = FabricaPlanta.buscarNoCatalogo(nomeProduto)
                    .orElseThrow(() -> invalido("produto \"" + nomeProduto + "\" não existe no catálogo"));
            long ordemChegada = lerLong(d, "ordemChegada");
            int quantidade = lerInt(filho(d, "quantidade"));
            int produzida = lerInt(filho(d, "quantidadeProduzida"));
            double tempo = lerDouble(filho(d, "tempoTotalProducao"));
            String textoStatus = lerTexto(filho(d, "status"));
            StatusDemanda status = validando(contexto, () -> StatusDemanda.valueOf(textoStatus));
            demandas.add(validando(contexto, () ->
                    Demanda.restaurar(modelo, quantidade, produzida, status, tempo, ordemChegada)));
        }
        return demandas;
    }

    // === Estrutura ===

    /**
     * O único filho direto c/ essa tag
     *
     * @param pai elemento pai
     * @param tag tag procurada
     * @return o filho
     * @throws ArquivoPersistenciaCorrompidoException se não tiver nenhum ou tiver mais de um
     */
    private Element filho(Element pai, String tag) {
        List<Element> encontrados = filhos(pai, tag);
        if (encontrados.isEmpty()) {
            throw corrompido("falta a tag <" + tag + "> dentro de <" + pai.getTagName() + ">");
        }
        if (encontrados.size() > 1) {
            throw corrompido("a tag <" + tag + "> aparece mais de uma vez dentro de <" + pai.getTagName() + ">");
        }
        return encontrados.getFirst();
    }

    /**
     * Todos os filhos diretos c/ essa tag, na ordem do arquivo
     *
     * @param pai elemento pai
     * @param tag tag procurada
     * @return filhos, pode ser vazia
     */
    private List<Element> filhos(Element pai, String tag) {
        List<Element> encontrados = new ArrayList<>();
        for (Node n = pai.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && e.getTagName().equals(tag)) {
                encontrados.add(e);
            }
        }
        return encontrados;
    }

    /**
     * Lista de partes iguais c/ ids 1, 2, 3... na ordem, ex.: os {@code <sensor>} dentro de {@code <sensores>}
     *
     * @param pai elemento que agrupa
     * @param tag tag de cada parte
     * @return as partes, na ordem
     * @throws DadosPersistidosInvalidosException se algum id estiver fora de ordem ou repetido
     */
    private List<Element> lerLista(Element pai, String tag) {
        List<Element> itens = filhos(pai, tag);
        for (int i = 0; i < itens.size(); i++) {
            int id = lerInt(itens.get(i), "id");
            if (id != i + 1) {
                throw invalido("<" + tag + "> número " + (i + 1) + " tem id " + id + " (os ids vão de 1 em diante, na ordem)");
            }
        }
        return itens;
    }

    private String atributo(Element el, String nome) {
        if (!el.hasAttribute(nome)) {
            throw corrompido("falta o atributo " + nome + " em <" + el.getTagName() + ">");
        }
        return el.getAttribute(nome);
    }

    private String lerTexto(Element el) {
        return el.getTextContent().trim();
    }

    private int lerInt(Element el) {
        return paraInt(lerTexto(el), "<" + el.getTagName() + ">");
    }

    private int lerInt(Element el, String atributo) {
        return paraInt(atributo(el, atributo), atributo + " de <" + el.getTagName() + ">");
    }

    private long lerLong(Element el, String atributo) {
        String texto = atributo(el, atributo);
        try {
            return Long.parseLong(texto.trim());
        } catch (NumberFormatException e) {
            throw corrompido(atributo + " de <" + el.getTagName() + "> não é um número inteiro: \"" + texto + "\"", e);
        }
    }

    private double lerDouble(Element el) {
        return paraDouble(lerTexto(el), "<" + el.getTagName() + ">");
    }

    private double lerDouble(Element el, String atributo) {
        return paraDouble(atributo(el, atributo), atributo + " de <" + el.getTagName() + ">");
    }

    /**
     * Lê um atributo true/false de cada elemento. Só aceita exatamente "true" ou "false"
     *
     * @param elementos elementos, na ordem
     * @param atributo atributo booleano
     * @return valores, na ordem
     */
    private List<Boolean> lerBooleans(List<Element> elementos, String atributo) {
        List<Boolean> valores = new ArrayList<>();
        for (Element el : elementos) {
            String texto = atributo(el, atributo).trim();
            if (!texto.equals("true") && !texto.equals("false")) {
                throw corrompido(atributo + " de <" + el.getTagName() + "> tem que ser true ou false: \"" + texto + "\"");
            }
            valores.add(Boolean.parseBoolean(texto));
        }
        return valores;
    }

    private int paraInt(String texto, String onde) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw corrompido(onde + " não é um número inteiro: \"" + texto + "\"", e);
        }
    }

    /**
     * Converte p/ double
     *
     * @param texto texto lido
     * @param onde onde ele estava, p/ a mensagem
     * @return o número
     */
    private double paraDouble(String texto, String onde) {
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            throw corrompido(onde + " não é um número: \"" + texto + "\"", e);
        }
    }

    // === Erros ===

    /**
     * Roda a criação/restauração de um objeto do domínio e transforma a recusa dele em
     * {@link DadosPersistidosInvalidosException}, dizendo em que parte do arquivo foi
     *
     * @param contexto onde está o dado
     * @param criacao código que cria ou restaura o objeto
     * @param <T> tipo do objeto
     * @return o objeto criado
     */
    private <T> T validando(String contexto, Supplier<T> criacao) {
        try {
            return criacao.get();
        } catch (PersistenciaException e) {
            throw e;
        } catch (IllegalArgumentException | PlantaException e) {
            throw new DadosPersistidosInvalidosException("Dessa vez não é! O save tem dados inválidos na " + contexto + ": "
                    + semBordao(e.getMessage()) + ".", e);
        }
    }

    /**
     * Tira o bordão do começo da mensagem do domínio, p/ ele não aparecer duas vezes quando ela vira o
     * detalhe da mensagem do leitor
     *
     * @param mensagem mensagem da exceção do domínio
     * @return a mensagem sem "Dessa vez não é!"/"Cadê a força?" no começo
     */
    private String semBordao(String mensagem) {
        String limpa = mensagem.replaceFirst("^(Dessa vez não é!|Cadê a força\\?)\\s*", "").strip();
        if (limpa.endsWith(".") || limpa.endsWith("!")) {
            limpa = limpa.substring(0, limpa.length() - 1);
        }
        return limpa.isEmpty() ? limpa : Character.toLowerCase(limpa.charAt(0)) + limpa.substring(1);
    }

    private ArquivoPersistenciaCorrompidoException corrompido(String detalhe) {
        return new ArquivoPersistenciaCorrompidoException("Dessa vez não é! O save está corrompido: " + detalhe + ".");
    }

    private ArquivoPersistenciaCorrompidoException corrompido(String detalhe, Throwable causa) {
        return new ArquivoPersistenciaCorrompidoException("Dessa vez não é! O save está corrompido: " + detalhe + ".", causa);
    }

    private DadosPersistidosInvalidosException invalido(String detalhe) {
        return new DadosPersistidosInvalidosException("Dessa vez não é! O save tem dados inválidos: " + detalhe + ".");
    }
}
