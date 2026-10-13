package view;

import domain.entities.productionmanager.ProductionManager;
import domain.entities.rawmaterial.RawMaterial;
import domain.exceptions.InsufficientBudgetException;

/**
 * Submenu de compra de matéria-prima. Mostra o estoque e o preço, pede a quantidade, mostra a verba
 * projetada e pede confirmação antes de comprar.
 *
 * @author Heitor Almeida
 */
public class BuyRawMaterialMenu extends Submenu {

    /**
     * exibe o submenu de compra
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção, onde a compra é feita
     */
    public BuyRawMaterialMenu(Menu menu, ProductionManager gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.VERDE);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "$");
    }

    @Override
    public String rotulo() {
        return "Comprar matéria-prima";
    }

    /**
     * Loop da compra. Quantidade negativa, orçamento insuficiente ou compra cancelada voltam pro início
     * c/ uma mensagem no rodapé
     */
    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            RawMaterial mp = gerenciadorProducao.getMateriaPrima();
            System.out.printf("   Item: " + ConsolePrinter.colorir(cor, "%s") + "\n", mp.getName());
            System.out.printf("   Estoque atual: " + ConsolePrinter.colorir(cor, "%.2f %s") + "\n", mp.getQuantity(), mp.getUnit());
            System.out.printf("   Custo unitário: " + ConsolePrinter.colorir(cor, "R$ %.2f / %s") + "\n\n", mp.getPrice(), mp.getUnit());
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();
            int quantidade = menu.readInt("Quantos " + mp.getUnit() + " deseja " + ConsolePrinter.colorir(cor, "COMPRAR") + "? ");

            if (quantidade == 0) break;

            if (quantidade < 0) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Quantidade inválida!"));
                continue;
            }

            float custoTotal = quantidade * mp.getPrice();
            if (custoTotal > gerenciadorProducao.getVerba()) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Orçamento insuficiente: a compra custa R$ %.2f e o orçamento é R$ %.2f.",
                        custoTotal, gerenciadorProducao.getVerba()));
                continue;
            }

            System.out.printf("\nVerba projetada: R$ %.2f " + ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "(▾ R$ -%.2f)") + "\n",
                    gerenciadorProducao.getVerba() - custoTotal, custoTotal);
            int confirmacao = menu.readInt("Confirmar compra? [" + ConsolePrinter.colorir(ConsolePrinter.VERDE, "1") + "] Sim / ["
                    + ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "0") + "] Não: ");

            if (confirmacao != 1) {
                menu.setLastBuffer(ConsolePrinter.colorir(cor, "Compra cancelada."));
                continue;
            }

            try {
                float valorPago = gerenciadorProducao.buyRawMaterial(quantidade);
                if (valorPago >= 150) {
                    menu.setLastBuffer(ConsolePrinter.textoOk("Dessa vez não é! Cliente comprou 150 reais e, sim, cliente ganhou um balão de presente!"));
                } else {
                    menu.setLastBuffer(ConsolePrinter.textoOk("Eitcha!!! Compra de matéria-prima realizada com sucesso!"));
                }
                break;
            } catch (InsufficientBudgetException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("%s", e.getMessage()));
            }
        }
    }
}
