package domain.entities.machine;

import domain.entities.product.Produto;
import domain.entities.product.StatusProduto;
import domain.utils.GeradorAleatorio;

/**
 * Última etapa da linha: decide se o produto vai pro armazém, vai c/ ressalva ou é descartado
 *
 * @author Heitor Almeida
 */
public class MaquinaInspecao extends Maquina {

    /**
     * Cria a máquina de inspeção
     *
     * @param nome nome que aparece nos menus
     * @param capacidadeMaxima unidades por ciclo
     * @param chanceFalhaBase chance base de falha, entre 0 e 1
     * @param custoOperacao custo p/ processar uma unidade, em R$
     * @param multiplicadorCenario multiplicador de falha do cenário
     * @param desgasteMaximo desgaste máximo por uso
     */
    public MaquinaInspecao(String nome, int capacidadeMaxima, double chanceFalhaBase,
                             double custoOperacao, double multiplicadorCenario,
                             int desgasteMaximo) {
        super(nome, capacidadeMaxima, chanceFalhaBase, custoOperacao, multiplicadorCenario, desgasteMaximo);
    }

    /**
     * Decide se o produto é rejeitado na inspeção. São dois sorteios independentes, basta um dar certo:
     * <ul>
     *   <li>a própria máquina falha, c/ chance {@link #getChanceFalha()} (já considera a saúde e o cenário).
     *   Funciona como um piso: mesmo um produto sem risco nenhum é rejeitado pelo menos c/ essa chance;</li>
     *   <li>o risco do produto se concretiza, c/ chance {@link Produto#getRiscoRejeicao()} vezes o multiplicador
     *   do cenário. Quanto maior a qualidade e o risco acumulado nas etapas anteriores, maior a chance.</li>
     * </ul>
     *
     * @param produto O produto sendo inspecionado.
     * @return true se o produto deve ser rejeitado
     */
    private boolean vaiSerRejeitado(Produto produto) {
        boolean maquinaFalhou = sorteiaFalha();

        double chanceRejeicao = produto.getRiscoRejeicao() * getMultiplicadorCenario();

        return maquinaFalhou || (GeradorAleatorio.chance(chanceRejeicao));
    }

    /**
     * Dá o veredito do produto: {@link StatusProduto#REJEITADO} se passou do limite de descarte ou foi rejeitado,
     * {@link StatusProduto#PRECISA_MANUTENCAO} se precisa de manutenção e {@link StatusProduto#APROVADO} se tá tudo certo
     *
     * @param produto produto vindo do empacotamento
     * @return o mesmo produto, c/ o status do veredito
     */
    @Override
    public Produto processarEtapa(Produto produto) {
        if (produto.passouLimiteDescarte() || vaiSerRejeitado(produto)) {
            produto.setStatus(StatusProduto.REJEITADO);
            return produto;
        }

        if (produto.precisaManutencao()) {
            produto.setStatus(StatusProduto.PRECISA_MANUTENCAO);
            return produto;
        }

        produto.setStatus(StatusProduto.APROVADO);
        return produto;
    }

    @Override
    public String getTipo() {
        return "Inspeção";
    }
}
