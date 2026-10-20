package domain.entities.productionline;

import domain.entities.machine.Maquina;
import domain.entities.productionmanager.EtapaProducao;
import domain.exceptions.MaquinaIndisponivelException;
import domain.interfaces.Auditavel;
import view.ConsolePrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * Classe da linha de produção da planta.
 */
public class LinhaDeProducao implements Auditavel {
    /// Nome da linha de producao
    private final String nome;
    /// Lista de máquinas da linha de producao
    private final List<Maquina> maquinas = new ArrayList<>();

    /**
     * Cria a linha vazia.
     *
     * @param nome nome da linha, aparece na auditoria estrutural
     * @throws IllegalArgumentException se o nome for nulo ou vazio
     */
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
     * Repara a máquina escolhida. O reparo é por demanda: vale p/ qualquer máquina c/ alguma parte
     * desgastada, sem esperar ela quebrar.
     *
     * @param indice O índice da máquina a ser reparada
     * @throws MaquinaIndisponivelException se todas as partes da máquina estiverem novas
     */
    public void repararMaquina(int indice) {
        if (indice < 0 || indice >= maquinas.size()) {
            throw new IllegalArgumentException("Índice de máquina inválido: " + indice);
        }
        Maquina m = maquinas.get(indice);
        if (!m.temPartesDesgastadas()) {
            throw new MaquinaIndisponivelException("Não foi dessa vez! A " + m.getNome() + " não precisa de reparo.");
        }
        m.reparar();
    }

    /**
     * Repara de uma vez todas as máquinas c/ alguma parte desgastada. As máquinas novas ficam como estão
     *
     * @return quantas máquinas foram reparadas
     * @throws MaquinaIndisponivelException se nenhuma máquina tiver desgaste
     */
    public int repararTodas() {
        int reparadas = 0;
        for (Maquina m : maquinas) {
            if (m.temPartesDesgastadas()) {
                m.reparar();
                reparadas++;
            }
        }
        if (reparadas == 0) {
            throw new MaquinaIndisponivelException("Não foi dessa vez! Todas as máquinas da " + nome.toLowerCase() + " estão novas.");
        }
        return reparadas;
    }

    // GETTERS

    /**
     * Nome da linha.
     *
     * @return o nome
     */
    public String getNome() {
        return nome;
    }

    // AUDITÁVEL

    /**
     * Auditoria em cascata completa: a linha lista suas máquinas e cada máquina se diagnostica (c/ as partes)
     *
     * @return árvore Linha > Máquina > Partes, c/ todas as máquinas expandidas
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        return gerarRelatorioDiagnostico(indice -> true);
    }

    /**
     * Auditoria em cascata c/ só algumas máquinas expandidas.
     *
     * @param expandir diz, pelo índice da máquina, se ela aparece expandida
     * @return árvore Linha > Máquina > Partes
     */
    public String gerarRelatorioDiagnostico(IntPredicate expandir) {
        StringBuilder text = new StringBuilder(String.format("%s\n", nome));
        for (int i = 0; i < maquinas.size(); i++) {
            Maquina m = maquinas.get(i);
            boolean ultima = (i == maquinas.size() - 1);
            String conector = ultima ? "└─" : "├─";
            String prefixo = ultima ? "   " : "│  ";

            text.append(String.format("%s %d. %-26s %s\n", conector, i + 1, m.getNome(), ConsolePrinter.estadoMaquina(m)));
            if (expandir.test(i)) {
                for (String linhaMaquina : m.gerarRelatorioDiagnostico().split("\n")) {
                    text.append(prefixo).append("   ").append(linhaMaquina).append("\n");
                }
            }
        }
        return text.toString();
    }

    @Override
    public String gerarResumoProblema() {
        StringBuilder text = new StringBuilder(String.format("%s\n", nome));
        int i = 1;
        for (Maquina m: maquinas) {
            String temp = String.format(" %d. %s\n", i++, m.gerarResumoProblema());
            text.append(temp);
        }
        return text.toString();
    }

    /**
     * Verifica se há alguma máquina quebrada na linha de produção.
     * @return true se alguma máquina precisar de manutenção
     */
    @Override
    public boolean precisaManutencao() {
        for (Maquina m: maquinas) {
            if (m.precisaManutencao()) {
                return true;
            }
        }
        return false;
    }
}