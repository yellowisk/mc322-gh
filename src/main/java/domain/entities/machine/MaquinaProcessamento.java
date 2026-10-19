package domain.entities.machine;

import domain.entities.product.Produto;
import domain.entities.product.StatusProduto;

import java.util.ArrayList;
import java.util.List;

/**
 * Primeira etapa da linha: derrete a matéria-prima no forno e transforma em um produto novo.
 * Além do motor e dos sensores, tem as {@link ResistenciaDoForno resistências do forno} (1:N).
 * Cada resistência queimada deixa o forno mais frio e o vidro sai c/ mais risco.
 * Se todas estiverem queimadas, a máquina para
 *
 * @author Heitor Almeida
 */
public class MaquinaProcessamento extends Maquina {
    /** Potência de cada resistência */
    private static final double POTENCIA_RESISTENCIA = 1500;
    /** Chance extra de falha c/ o forno totalmente frio */
    private static final double CHANCE_EXTRA_FORNO_FRIO = 0.3;
    private final List<ResistenciaDoForno> resistencias;

    /**
     * Cria a máquina de processamento c/ o forno funcionando
     *
     * @param nome nome que aparece nos menus
     * @param capacidadeMaxima unidades por ciclo
     * @param chanceFalhaBase chance base de falha, entre 0 e 1
     * @param custoOperacao custo p/ processar uma unidade, em R$
     * @param multiplicadorCenario multiplicador de falha do cenário
     * @param desgasteMaximo desgaste máximo do motor por uso
     * @param qtdSensores quantos sensores a máquina tem
     * @param qtdResistencias quantas resistências o forno tem
     * @throws IllegalArgumentException se alguma quantidade não for positiva
     */
    public MaquinaProcessamento(String nome, int capacidadeMaxima, double chanceFalhaBase,
                                double custoOperacao, double multiplicadorCenario,
                                int desgasteMaximo, int qtdSensores, int qtdResistencias) {
        super(nome, capacidadeMaxima, chanceFalhaBase, custoOperacao, multiplicadorCenario, desgasteMaximo, qtdSensores);
        if (qtdResistencias < 1) {
            throw new IllegalArgumentException("Dessa vez não é! O forno precisa de pelo menos uma resistência.");
        }
        this.resistencias = new ArrayList<>();
        for (int i = 1; i <= qtdResistencias; i++) {
            this.resistencias.add(new ResistenciaDoForno(i, POTENCIA_RESISTENCIA));
        }
    }

    /**
     * Aquece o forno e cria uma unidade nova a partir do modelo do catálogo, já como {@link StatusProduto#PROCESSADO}.
     * Se a máquina falhar no sorteio, a unidade sai c/ mais risco acumulado. Quanto mais potência o forno
     * perdeu c/ resistências queimadas, maior a chance de falha
     *
     * @param produto modelo do catálogo
     * @return unidade nova, c/ id próprio
     */
    @Override
    protected Produto processarEtapa(Produto produto) {
        this.resistencias.forEach(ResistenciaDoForno::aquecer);

        Produto novoProduto = produto.processar(produto, StatusProduto.PROCESSADO);
        tentaAumentarRisco(novoProduto);
        return novoProduto;
    }

    /**
     * Quanto da potência do forno foi perdida c/ resistências queimadas
     *
     * @return de 0 (todas funcionando) a 1 (todas queimadas)
     */
    private double fracaoPotenciaPerdida() {
        double total = 0;
        double entregue = 0;
        for (ResistenciaDoForno r : this.resistencias) {
            total += r.getPotencia();
            entregue += r.potenciaEntregue();
        }
        return 1 - entregue / total;
    }

    /**
     * Forno mais frio deixa o vidro mal derretido: a chance extra cresce c/ a potência perdida
     *
     * @return de 0 (todas funcionando) 0.3 (todas queimadas)
     */
    @Override
    protected double chanceExtraPartesEspecificas() {
        return fracaoPotenciaPerdida() * CHANCE_EXTRA_FORNO_FRIO;
    }

    /** Troca todas as resistências queimadas */
    @Override
    protected void repararPartesEspecificas() {
        this.resistencias.forEach(ResistenciaDoForno::trocar);
    }

    /**
     * Confirma se todas as resistências queimaram. O forno só para quando todas
     * as resistências queimam
     *
     * @return true se todas estiverem queimadas
     */
    @Override
    protected boolean partesEspecificasQuebradas() {
        return this.resistencias.stream().allMatch(ResistenciaDoForno::estaQueimada);
    }

    @Override
    protected List<String> diagnosticoPartesEspecificas() {
        List<String> linhas = new ArrayList<>();
        for (ResistenciaDoForno r : this.resistencias) {
            linhas.add(linhaComponente(!r.estaQueimada(), r.diagnostico()));
        }
        return linhas;
    }

    @Override
    protected List<String> diagnosticoPartesEspecificasDesgastadas() {
        List<String> linhas = new ArrayList<>();
        for (ResistenciaDoForno r : this.resistencias) {
            if (r.estaQueimada()) {
                linhas.add(r.diagnostico());
            }
        }
        return linhas;
    }

    @Override
    public String getTipo() {
        return "Processamento";
    }
}
