package view;

import domain.entities.demand.Demanda;
import domain.entities.productionmanager.GerenciadorProducao;
import domain.exceptions.PlantaException;

import java.util.List;

/**
 * Submenu de fabricação. Lista as demandas e deixa escolher uma p/ fabricar, ou fabricar a próxima
 * que a estratégia atual escolher
 *
 * @author Heitor Almeida
 */
public class FabricateDemandMenu extends Submenu {

    /**
     * Cria o submenu c/ a cor roxa
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     */
    public FabricateDemandMenu(Menu menu, GerenciadorProducao gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.ROXO);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "⚙");
    }

    @Override
    public String rotulo() {
        return "Fabricar demandas";
    }

    /**
     * Loop do submenu. Depois de uma fabricação espera o ENTER e volta pro menu principal. Opção inválida
     * ou qualquer erro de domínio antes da fabricação começar ({@link domain.exceptions.PlantaException}:
     * orçamento insuficiente, máquina quebrada, demanda inválida...) voltam pra lista c/ a mensagem no rodapé,
     * sem nada ter sido alterado
     */
    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            List<Demanda> demandas = gerenciadorProducao.getDemandas();
            ConsolePrinter.listarDemandas(gerenciadorProducao);

            // A opção logo depois das demandas fabrica o que a estratégia atual escolher
            int proximaPelaEstrategia = demandas.size() + 1;
            Demanda proxima = gerenciadorProducao.espiarProximaDemanda();
            String previa;
            if (proxima == null) {
                previa = ConsolePrinter.colorir(ConsolePrinter.CINZA, "nenhuma demanda elegível");
            } else {
                previa = proxima.getNomeProduto() + " (" + proxima.getQuantidade() + " un)";
                if (!proxima.ehViavel(gerenciadorProducao.getVerba())) {
                    previa += " " + ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "(orçamento insuficiente)");
                }
            }
            System.out.printf(ConsolePrinter.CINZA + " %d." + ConsolePrinter.RESET + " " + ConsolePrinter.colorir(cor, "▶")
                            + " Próxima pela estratégia " + ConsolePrinter.colorir(cor, "%s") + ": %s\n\n",
                    proximaPelaEstrategia, gerenciadorProducao.getEstrategiaAtual().getNomeEstrategia(), previa);
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();
            int opcao = menu.readInt("Qual demanda deseja " + ConsolePrinter.colorir(cor, "FABRICAR") + "? ");

            if (opcao == 0) break;

            try {
                if (opcao == proximaPelaEstrategia) {
                    gerenciadorProducao.iniciarProducao();
                } else {
                    Menu.validarOpcao(opcao, demandas.size(), "Dessa vez não é! Opção inválida!");
                    gerenciadorProducao.produzirDemanda(demandas.get(opcao - 1));
                }
            } catch (PlantaException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("%s", e.getMessage()));
                continue;
            }

            menu.waitEnter();
            break;
        }
    }
}
