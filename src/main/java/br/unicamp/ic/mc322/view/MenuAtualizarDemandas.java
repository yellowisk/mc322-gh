package br.unicamp.ic.mc322.view;

import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.exception.OpcaoInvalidaException;

/**
 * Submenu de atualização de demandas. Escolhe uma demanda e define a nova quantidade, que reseta
 * o progresso dela e dá uma nova ordem de chegada
 *
 * @author Heitor Almeida
 */
public class MenuAtualizarDemandas extends Submenu {

    /**
     * Cria o submenu c/ a cor amarela
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     */
    public MenuAtualizarDemandas(Menu menu, GerenciadorProducao gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.AMARELO);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "⟳");
    }

    @Override
    public String rotulo() {
        return "Atualizar demandas";
    }

    /**
     * Loop do submenu. Quantidade negativa ou opção inválida voltam pra lista c/ a mensagem no rodapé
     */
    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            ConsolePrinter.listarDemandas(gerenciadorProducao);
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.imprimirRodape();
            int opcao = menu.lerInteiro("Qual demanda deseja " + ConsolePrinter.colorir(cor, "ATUALIZAR") + "? ");

            if (opcao == 0) break;

            try {
                Menu.validarOpcao(opcao, gerenciadorProducao.getDemandas().size(), "Dessa vez não é! Opção inválida!");
            } catch (OpcaoInvalidaException e) {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("%s", e.getMessage()));
                continue;
            }
            Demanda demandaEscolhida = gerenciadorProducao.getDemandas().get(opcao - 1);

            int novoValor = menu.lerInteiro("Digite o novo valor da demanda: ");
            try {
                gerenciadorProducao.atualizarDemanda(demandaEscolhida, novoValor);
                menu.setUltimaMensagem(ConsolePrinter.textoOk("Demanda de %s atualizada para %d!", demandaEscolhida.getNomeProduto(), novoValor));
            } catch (PlantaException e) {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("Dessa vez não é! %s", e.getMessage()));
            }
        }
    }
}
