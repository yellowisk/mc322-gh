package domain.entities.machine;

import domain.entities.product.Produto;
import domain.entities.product.StatusProduto;
import domain.utils.GeradorAleatorio;

import java.util.ArrayList;
import java.util.List;

/**
 * Última etapa da linha: decide se o produto vai pro armazém, vai c/ ressalva ou é descartado.
 * Além do motor e dos sensores, tem as {@link CameraDeInspecao câmeras de inspeção} (1:N). Câmera c/ a lente
 * suja não enxerga defeito, então produtos defeituosos passam como aprovados; todas sujas, a máquina para
 *
 * @author Heitor Almeida
 */
public class MaquinaInspecao extends Maquina {
    private final List<CameraDeInspecao> cameras;

    /**
     * Cria a máquina de inspeção
     *
     * @param nome nome que aparece nos menus
     * @param capacidadeMaxima unidades por ciclo
     * @param chanceFalhaBase chance base de falha, entre 0 e 1
     * @param custoOperacao custo p/ processar uma unidade, em R$
     * @param multiplicadorCenario multiplicador de falha do cenário
     * @param desgasteMaximo desgaste máximo do motor por uso
     * @param qtdSensores quantos sensores a máquina tem
     * @param qtdCameras quantas câmeras a máquina tem
     * @throws IllegalArgumentException se alguma quantidade não for positiva
     */
    public MaquinaInspecao(String nome, int capacidadeMaxima, double chanceFalhaBase,
                           double custoOperacao, double multiplicadorCenario,
                           int desgasteMaximo, int qtdSensores, int qtdCameras) {
        super(nome, capacidadeMaxima, chanceFalhaBase, custoOperacao, multiplicadorCenario, desgasteMaximo, qtdSensores);
        if (qtdCameras < 1) {
            throw new IllegalArgumentException("Dessa vez não é! A inspeção precisa de pelo menos uma câmera.");
        }
        this.cameras = new ArrayList<>();
        for (int i = 1; i <= qtdCameras; i++) {
            this.cameras.add(new CameraDeInspecao(i));
        }
    }

    /**
     * Decide se o produto é rejeitado na inspeção. São dois sorteios independentes, basta um dar certo:
     * <ul>
     *   <li>a própria máquina falha, c/ chance {@link #getChanceFalha()} (já considera motor, sensores e cenário).
     *   Funciona como um piso: mesmo um produto sem risco nenhum é rejeitado pelo menos c/ essa chance;</li>
     *   <li>as câmeras pegam o risco do produto, c/ chance {@link Produto#getRiscoRejeicao()} vezes o
     *   multiplicador do cenário vezes a fração de câmeras funcionando. Quanto mais câmeras sujas,
     *   mais produtos defeituosos passariam.</li>
     * </ul>
     *
     * @param produto O produto sendo inspecionado.
     * @return true se o produto deve ser rejeitado
     */
    private boolean vaiSerRejeitado(Produto produto) {
        boolean maquinaFalhou = sorteiaFalha();

        double chanceRejeicao = produto.getRiscoRejeicao() * getMultiplicadorCenario() * fracaoCamerasFuncionando();

        return maquinaFalhou || (GeradorAleatorio.chance(chanceRejeicao));
    }

    /**
     * Quantas câmeras ainda enxergam, em relação ao total
     *
     * @return de 0 (todas sujas) a 1 (todas limpas)
     */
    private double fracaoCamerasFuncionando() {
        long funcionando = this.cameras.stream().filter(CameraDeInspecao::estaFuncionando).count();
        return (double) funcionando / this.cameras.size();
    }

    /**
     * Captura o produto com as câmeras e dá o veredito: {@link StatusProduto#REJEITADO} se passou do limite de
     * descarte ou foi rejeitado, {@link StatusProduto#PRECISA_MANUTENCAO} se precisa de manutenção e
     * {@link StatusProduto#APROVADO} se tá tudo certo
     *
     * @param produto produto vindo do empacotamento
     * @return o mesmo produto, c/ o status do veredito
     */
    @Override
    protected Produto processarEtapa(Produto produto) {
        this.cameras.forEach(CameraDeInspecao::capturar);

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

    /**
     * Câmera suja não faz a máquina falhar, mas deixa defeitos passarem, o que já entra na chance de
     * rejeição da inspeção
     *
     * @return sempre 0
     */
    @Override
    protected double chanceExtraPartesEspecificas() {
        return 0;
    }

    /** Limpa a lente de todas as câmeras */
    @Override
    protected void repararPartesEspecificas() {
        this.cameras.forEach(CameraDeInspecao::limpar);
    }

    /**
     * A máquina só para quando nenhuma câmera enxerga mais
     *
     * @return true se todas as câmeras estiverem c/ a lente suja
     */
    @Override
    protected boolean partesEspecificasQuebradas() {
        return this.cameras.stream().noneMatch(CameraDeInspecao::estaFuncionando);
    }

    @Override
    protected List<String> diagnosticoPartesEspecificas() {
        List<String> linhas = new ArrayList<>();
        for (CameraDeInspecao c : this.cameras) {
            linhas.add(linhaComponente(c.estaFuncionando(), c.diagnostico()));
        }
        return linhas;
    }

    @Override
    protected List<String> diagnosticoPartesEspecificasDesgastadas() {
        List<String> linhas = new ArrayList<>();
        for (CameraDeInspecao c : this.cameras) {
            if (!c.estaFuncionando()) {
                linhas.add(c.diagnostico());
            }
        }
        return linhas;
    }

    @Override
    public String getTipo() {
        return "Inspeção";
    }
}
