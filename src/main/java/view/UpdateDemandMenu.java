package view;

import domain.entities.demand.Demand;
import domain.entities.productionmanager.ProductionManager;
import domain.exceptions.InvalidDemandAmountException;

public class UpdateDemandMenu extends Submenu {

    public UpdateDemandMenu(Menu menu, ProductionManager gerenciadorProducao) {
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

    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            ConsolePrinter.listarDemandas(gerenciadorProducao);
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();
            int option = menu.readInt("Qual demanda deseja " + ConsolePrinter.colorir(cor, "ATUALIZAR") + "? ");

            if (option == 0) break;

            Demand selectedDemand;
            try {
                selectedDemand = gerenciadorProducao.getDemands().get(option - 1);
            } catch (IndexOutOfBoundsException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
                continue;
            }

            int newValue = menu.readInt("Digite o novo valor da demanda: ");
            try {
                gerenciadorProducao.updateDemand(selectedDemand, newValue);
                menu.setLastBuffer(ConsolePrinter.textoOk("Demanda de %s atualizada para %d!", selectedDemand.getProductName(), newValue));
            } catch (InvalidDemandAmountException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! %s", e.getMessage()));
            }
        }
    }
}
