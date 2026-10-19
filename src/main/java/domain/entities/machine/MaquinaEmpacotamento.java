package domain.entities.machine;

import domain.entities.product.Produto;
import domain.entities.product.StatusProduto;

import java.util.List;

/**
 * Segunda etapa da linha: empacota o produto que saiu do processamento.
 * Além do motor e dos sensores, tem uma {@link Seladora} (1:1). Quanto mais gasta a lâmina, maior a
 * chance de a embalagem sair mal selada; gasta de vez, a máquina para
 *
 * @author Heitor Almeida
 */
public class MaquinaEmpacotamento extends Maquina {
    private final Seladora seladora;

    /**
     * Cria a máquina de empacotamento
     *
     * @param nome nome que aparece nos menus
     * @param capacidadeMaxima unidades por ciclo
     * @param chanceFalhaBase chance base de falha, entre 0 e 1
     * @param custoOperacao custo p/ processar uma unidade, em R$
     * @param multiplicadorCenario multiplicador de falha do cenário
     * @param desgasteMaximo desgaste máximo do motor por uso
     * @param qtdSensores quantos sensores a máquina tem
     * @throws IllegalArgumentException se a quantidade de sensores não for positiva
     */
    public MaquinaEmpacotamento(String nome, int capacidadeMaxima, double chanceFalhaBase,
                                double custoOperacao, double multiplicadorCenario,
                                int desgasteMaximo, int qtdSensores) {
        super(nome, capacidadeMaxima, chanceFalhaBase, custoOperacao, multiplicadorCenario, desgasteMaximo, qtdSensores);
        this.seladora = new Seladora();
    }

    /**
     * Sela e marca o produto como {@link StatusProduto#EMPACOTADO}. Se a máquina falhar no sorteio,
     * o produto sai c/ mais risco acumulado. A lâmina gasta soma a chance de selagem ruim com a de falha
     *
     * @param produto produto vindo do processamento
     * @return o mesmo produto, empacotado
     */
    @Override
    protected Produto processarEtapa(Produto produto) {
        this.seladora.selar();
        tentaAumentarRisco(produto);

        produto.setStatus(StatusProduto.EMPACOTADO);
        return produto;
    }

    /**
     * Lâmina gasta deixa a embalagem mal selada
     *
     * @return chance de selagem ruim da seladora
     */
    @Override
    protected double chanceExtraPartesEspecificas() {
        return this.seladora.chanceSelagemRuim();
    }

    /** Troca a lâmina da seladora */
    @Override
    protected void repararPartesEspecificas() {
        this.seladora.trocarLamina();
    }

    /**
     * A máquina para quando a lâmina gasta de vez
     *
     * @return true se a lâmina estiver totalmente gasta
     */
    @Override
    protected boolean partesEspecificasQuebradas() {
        return this.seladora.estaGasta();
    }

    @Override
    protected List<String> diagnosticoPartesEspecificas() {
        return List.of(linhaComponente(!this.seladora.estaGasta(), this.seladora.diagnostico()));
    }

    @Override
    protected List<String> diagnosticoPartesEspecificasDesgastadas() {
        return this.seladora.estaNova() ? List.of() : List.of(this.seladora.diagnostico());
    }

    @Override
    public String getTipo() {
        return "Empacotamento";
    }
}
