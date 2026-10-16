package domain.entities.productionline;

import domain.entities.machine.Maquina;
import domain.entities.productionmanager.EtapaProducao;
import domain.exceptions.MaquinaIndisponivelException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe da linha de produção da planta.
 */
public class LinhaDeProducao {
    /// Nome da linha de producao
    private final String nome;
    /// Lista de máquinas da linha de producao
    private final List<Maquina> maquinas = new ArrayList<>();

    public LinhaDeProducao(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da linha de produção não pode ser nulo ou vazio.");
        }
        this.nome = nome;
    }

    /**
     * Adiciona uma máquina à linha de produção.
     * @param m a máquina a ser adicionada
     */
    public void adicionarMaquina(Maquina m) {
        if (m == null) {
            throw new IllegalArgumentException("A máquina não pode ser nula.");
        }
        maquinas.add(m);
    }

    /**
     * Retorna a lista de máquinas em forma imutável.
     * @return a lista de máquinas
     */
    public List<Maquina> getMaquinas() {
        return Collections.unmodifiableList(maquinas);
    }

    /**
     * Retorna a máquina especificada pelo índice
     * @param indice o índice da lista
     * @return a maquina
     */
    public Maquina getMaquina(int indice) {
        return maquinas.get(indice);
    }

    /**
     * Retorna a máquina de uma etapa específica.
     * @param etapa etapa
     * @return a máquina da etapa especificada
     */
    public Maquina getMaquinaDaEtapa(EtapaProducao etapa) {
        return maquinas.get(etapa.getCodigo());
    }

    /**
     * Liga todas as máquinas da linha de produção.
     */
    public void ligar() {
        maquinas.forEach(Maquina::ligar);
    }

    /**
     * Desliga todas as máquinas da linha de produção.
     */
    public void desligar() {
        maquinas.forEach(Maquina::desligar);
    }

    /**
     * Verifica se há alguma máquina quebrada na linha de produção.
     * @return true se alguma máquina precisar de manutenção
     */
    public boolean temMaquinaQuebrada() {
        return maquinas.stream().anyMatch(Maquina::precisaManutencao);
    }

    /**
     * Calcula o custo total de operação unitária da linha somando
     * o custo individual de cada máquina.
     * @return custo total de uma unidade de produto
     */
    public double calcCustoOperacaoPorUnidade() {
        double soma = 0;
        for (Maquina m : maquinas) {
            soma += m.getCustoOperacao();
        }
        return soma;
    }

    /**
     * Repara a máquina escolhida.
     * @param indice O índice da máquina a ser reparada
     * @throws MaquinaIndisponivelException se a máquina não precisar de reparo
     */
    public void repararMaquina(int indice) {
        if (indice < 0 || indice >= maquinas.size()) {
            throw new IllegalArgumentException("Índice de máquina inválido: " + indice);
        }
        Maquina m = maquinas.get(indice);
        if (!m.precisaManutencao()) {
            throw new MaquinaIndisponivelException("Não foi dessa vez! A " + m.getNome() + " não precisa de reparo.");
        }
        m.reparar();
    }

    // GETTERS

    public String getNome() {
        return nome;
    }
}