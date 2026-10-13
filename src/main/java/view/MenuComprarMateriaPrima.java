package view;

import domain.entities.productionmanager.GerenciadorProducao;
import domain.entities.rawmaterial.MateriaPrima;
import domain.exceptions.OrcamentoInsuficienteException;

/**
 * Submenu de compra de matéria-prima. Mostra o estoque e o preço, pede a quantidade, mostra a verba
 * projetada e pede confirmação antes de comprar.
 *
 * @author Heitor Almeida
 */
public class MenuComprarMateriaPrima extends Submenu {

    /**
     * exibe o submenu de compra
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção, onde a compra é feita
     */
    public MenuComprarMateriaPrima(Menu menu, GerenciadorProducao gerenciadorProducao) {
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
            MateriaPrima mp = gerenciadorProducao.getMateriaPrima();
            System.out.printf("   Item: " + ConsolePrinter.colorir(cor, "%s") + "\n", mp.getNome());
            System.out.printf("   Estoque atual: " + ConsolePrinter.colorir(cor, "%.2f %s") + "\n", mp.getQuantidade(), mp.getUnidade());
            System.out.printf("   Custo unitário: " + ConsolePrinter.colorir(cor, "R$ %.2f / %s") + "\n\n", mp.getPreco(), mp.getUnidade());
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.imprimirRodape();
            int quantidade = menu.lerInteiro("Quantos " + mp.getUnidade() + " deseja " + ConsolePrinter.colorir(cor, "COMPRAR") + "? ");

            if (quantidade == 0) break;

            if (quantidade < 0) {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("Dessa vez não é! Quantidade inválida!"));
                continue;
            }

            float custoTotal = quantidade * mp.getPreco();
            if (custoTotal > gerenciadorProducao.getVerba()) {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("Dessa vez não é! Orçamento insuficiente: a compra custa R$ %.2f e o orçamento é R$ %.2f.",
                        custoTotal, gerenciadorProducao.getVerba()));
                continue;
            }

            System.out.printf("\nVerba projetada: R$ %.2f " + ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "(▾ R$ -%.2f)") + "\n",
                    gerenciadorProducao.getVerba() - custoTotal, custoTotal);
            int confirmacao = menu.lerInteiro("Confirmar compra? [" + ConsolePrinter.colorir(ConsolePrinter.VERDE, "1") + "] Sim / ["
                    + ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "0") + "] Não: ");

            if (confirmacao != 1) {
                menu.setUltimaMensagem(ConsolePrinter.colorir(cor, "Compra cancelada."));
                continue;
            }

            try {
                float valorPago = gerenciadorProducao.comprarMateriaPrima(quantidade);
                if (valorPago >= 150) {
                    menu.setUltimaMensagem(ConsolePrinter.textoOk("Dessa vez não é! Cliente comprou 150 reais e, sim, cliente ganhou um balão de presente!"));
                } else {
                    menu.setUltimaMensagem(ConsolePrinter.textoOk("Eitcha!!! Compra de matéria-prima realizada com sucesso!"));
                }
                break;
            } catch (OrcamentoInsuficienteException e) {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("%s", e.getMessage()));
            }
        }
    }
}
