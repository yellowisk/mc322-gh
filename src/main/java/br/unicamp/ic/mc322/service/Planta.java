package br.unicamp.ic.mc322.service;

import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.model.product.Produto;
import br.unicamp.ic.mc322.model.productionline.LinhaDeProducao;
import br.unicamp.ic.mc322.model.scenario.ConfiguracaoCenario;

/**
 * Estado completo de uma fábrica: o cenário, o gerenciador da produção e a linha. É o que se salva,
 * se carrega e se troca de uma vez. Montada pela {@link FabricaPlanta}
 *
 * @author Heitor Almeida
 */
public class Planta {
    private final ConfiguracaoCenario cenario;
    private final GerenciadorProducao gerenciador;
    private final LinhaDeProducao linha;

    /**
     * Junta as partes de uma fábrica já montada
     *
     * @param cenario cenário da fábrica
     * @param gerenciador gerenciador da produção, que já aponta p/ a {@code linha}
     * @param linha linha de produção c/ as máquinas
     */
    public Planta(ConfiguracaoCenario cenario, GerenciadorProducao gerenciador, LinhaDeProducao linha) {
        this.cenario = cenario;
        this.gerenciador = gerenciador;
        this.linha = linha;
    }

    /**
     * Ajusta o estado static compartilhado entre fábricas: o próximo id de produto volta p/ 1 e a próxima
     * demanda chega depois de todas as desta fábrica.<br>
     * Só deve ser chamado quando a fábrica vai passar a ser usada (montada e validada), porque mexe em
     * contadores que valem p/ o programa inteiro
     */
    public void ativar() {
        Produto.resetarContadorId();
        long ultimaChegada = gerenciador.getDemandas().stream()
                .mapToLong(Demanda::getOrdemChegada)
                .max()
                .orElse(-1);
        Demanda.ajustarProximaChegada(ultimaChegada + 1);
    }

    /**
     * Cenário da fábrica
     *
     * @return cenário
     */
    public ConfiguracaoCenario getCenario() {
        return cenario;
    }

    /**
     * Gerenciador da produção
     *
     * @return gerenciador
     */
    public GerenciadorProducao getGerenciador() {
        return gerenciador;
    }

    /**
     * Linha de produção c/ as máquinas
     *
     * @return linha
     */
    public LinhaDeProducao getLinha() {
        return linha;
    }
}
