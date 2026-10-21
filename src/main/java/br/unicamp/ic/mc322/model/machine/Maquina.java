package br.unicamp.ic.mc322.model.machine;

import br.unicamp.ic.mc322.model.product.Produto;
import br.unicamp.ic.mc322.exception.MaquinaPrecisaReparoException;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.model.Auditavel;
import br.unicamp.ic.mc322.util.GeradorAleatorio;
import br.unicamp.ic.mc322.view.ConsolePrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * Máquina de uma etapa da linha de produção. É o Todo de uma composição: cria e controla as próprias
 * partes, que ninguém fora dela enxerga. Toda máquina tem um {@link Motor} (1:1), que se desgasta a cada
 * {@link #processar(Produto)}, e um ou mais {@link Sensor}es (1:N), que perdem calibração c/ o uso.
 * Cada subtipo ainda adiciona uma parte específica da sua etapa.
 * <p>
 * Motor gasto e sensores descalibrados aumentam a chance de falha. Motor abaixo do limiar crítico, ou a
 * parte específica parada, quebram a máquina até ela ser reparada
 *
 * @author Heitor Almeida
 * @author Glayson Oliveira
 */
public abstract class Maquina implements Auditavel {
    // === Atributos ===
    private final String nome;
    private boolean ligada;
    private final int capacidadeMaxima;
    /** Chance base de falha. */
    private final double chanceFalhaBase;
    private final double custoOperacao;
    /** Multiplicador de falha do cenário atual */
    private static double multiplicadorCenario;

    // Partes
    private final Motor motor;
    private final List<Sensor> sensores;

    /**
     * Cria a máquina desligada, c/ o motor novo e os sensores calibrados
     *
     * @param nome nome que aparece nos menus, ex.: "Máquina de Inspeção"
     * @param capacidadeMaxima unidades por ciclo
     * @param chanceFalhaBase chance base de falha, entre 0 e 1
     * @param custoOperacao custo p/ processar uma unidade, em R$
     * @param multiplicadorCenario multiplicador de falha do cenário. É static, então vale p/ todas as máquinas
     * @param desgasteMaximo desgaste máximo do motor por uso, cada uso tira de 1 a esse valor da saúde
     * @param qtdSensores quantos sensores a máquina tem
     * @throws IllegalArgumentException se a quantidade de sensores ou o desgaste máximo não forem positivos
     */
    public Maquina(String nome, int capacidadeMaxima, double chanceFalhaBase,
                   double custoOperacao, double multiplicadorCenario,
                   int desgasteMaximo, int qtdSensores) {
        if (qtdSensores < 1) {
            throw new IllegalArgumentException("Dessa vez não é! A máquina precisa de pelo menos um sensor.");
        }
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.chanceFalhaBase = chanceFalhaBase;
        this.custoOperacao = custoOperacao;
        Maquina.multiplicadorCenario = multiplicadorCenario;

        this.motor = new Motor(desgasteMaximo);
        this.sensores = new ArrayList<>();
        for (int i = 1; i <= qtdSensores; i++) {
            this.sensores.add(new Sensor(i));
        }
    }

    // === HOOKS DOS SUBTIPOS ===

    /**
     * Método auxiliar do método 'processar()'. Esse método será
     * sobrescrito pelas subclasses de {@link Maquina}.
     *
     * @param produto O produto deverá ser fabricado.
     * @return Produto fabricado.
     */
    protected abstract Produto processarEtapa(Produto produto);

    /** Conserta a parte específica do subtipo. Chamado por {@link #reparar()} */
    protected abstract void repararPartesEspecificas();

    /**
     * Diz se a parte específica do subtipo parou de vez, o que impede a máquina de processar
     *
     * @return true se a parte específica precisa de reparo
     */
    protected abstract boolean partesEspecificasQuebradas();

    /**
     * Chance de falha que a parte específica do subtipo soma à da máquina.
     * Entra em {@link #getChanceFalha()}, então pesa no sorteio e aparece na auditoria
     *
     * @return chance extra, entre 0 e 1. 0 se a parte específica não muda a chance de falha
     */
    protected abstract double chanceExtraPartesEspecificas();

    /**
     * Linhas de diagnóstico da parte específica do subtipo, já formatadas c/ {@link #linhaComponente(boolean, String)}
     *
     * @return uma linha por parte
     */
    protected abstract List<String> diagnosticoPartesEspecificas();

    /**
     * Diagnóstico só das partes específicas do subtipo que não estão novas (gastas, sujas, queimadas...),
     * Usado p/ dizer o que um reparo vai consertar
     *
     * @return uma linha por parte desgastada, vazia se todas estiverem novas
     */
    protected abstract List<String> diagnosticoPartesEspecificasDesgastadas();

    /**
     * Tipo da máquina, usado nos logs da produção
     *
     * @return tipo, ex.: "Empacotamento"
     */
    public abstract String getTipo();

    // === PROCESSAMENTO ===

    /**
     * Confere se a máquina pode processar agora, sem mexer em nada. A produção chama isso antes de cobrar
     * a etapa, p/ não pagar por uma operação que não vai acontecer
     *
     * @throws MaquinaIndisponivelException se a máquina estiver desligada
     * @throws MaquinaPrecisaReparoException se o motor ou a parte específica estiverem quebrados
     */
    public void verificarDisponivel() {
        if (!estaLigada()) {
            String mensagem = String.format("Eitcha, João! A %s não pode processar nada, ela tá desligada!", getNome().toLowerCase());
            throw new MaquinaIndisponivelException(mensagem);
        }

        if (this.motor.estaQuebrado()) {
            String mensagem = String.format("O motor da %s quebrou! Eitcha!", getNome().toLowerCase());
            throw new MaquinaPrecisaReparoException(mensagem);
        }

        if (partesEspecificasQuebradas()) {
            String mensagem = String.format("Eitcha! A %s quebrou! Os componentes dela pararam de funcionar.", getNome().toLowerCase());
            throw new MaquinaPrecisaReparoException(mensagem);
        }
    }

    /**
     * Passa o produto pela etapa da máquina via {@link #processarEtapa(Produto)}, desgasta o motor e registra
     * o ciclo nos sensores. As checagens e o desgaste ficam aqui, a etapa em si fica em cada subclasse
     *
     * @param produto modelo do catálogo ou unidade vinda da etapa anterior
     * @return o produto depois da etapa
     * @throws MaquinaIndisponivelException se a máquina estiver desligada
     * @throws MaquinaPrecisaReparoException se o motor ou a parte específica estiverem quebrados
     */
    public Produto processar(Produto produto) {
        verificarDisponivel();

        Produto resultado = processarEtapa(produto);
        this.motor.desgastar();
        this.sensores.forEach(Sensor::registrarCiclo);
        return resultado;
    }

    /**
     * Sorteia se a máquina falhou nessa unidade, c/ a chance de {@link #getChanceFalha()} (que já inclui
     * a parte específica). Se falhou, o produto ganha 0.12 * multiplicador do cenário de risco acumulado,
     * que pesa na inspeção
     *
     * @param produto produto que pode sair c/ defeito
     */
    protected void tentaAumentarRisco(Produto produto) {
        if (GeradorAleatorio.chance(getChanceFalha())) {
            double incremento = 0.12 * multiplicadorCenario;
            produto.aumentarProbabilidadeFalha(incremento);
        }
    }

    // === SAÚDE DA MÁQUINA ===

    /**
     * Repara a máquina inteira: motor c/ saúde máxima, sensores recalibrados e parte específica consertada
     */
    public void reparar() {
        this.motor.reparar();
        this.sensores.forEach(Sensor::recalibrar);
        repararPartesEspecificas();
    }

    // === AUDITÁVEL ===

    /**
     * Retorna um relatório de diagnóstico formatado c/ o estado da máquina (status, chance de falha, custos).
     *
     * @return String formatada com o diagnóstico.
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        String reset = ConsolePrinter.RESET;
        String azul = ConsolePrinter.AZUL;
        String verde = ConsolePrinter.VERDE;
        String amarelo = ConsolePrinter.AMARELO;
        String cinza = ConsolePrinter.CINZA;

        // Chance de falha
        String chanceFalhaFormatada;
        double diffChanceFalha = getChanceFalha() - getChanceFalhaCenario();
        if (Math.abs(diffChanceFalha) < 1e-9) {
            chanceFalhaFormatada = String.format("%s%.0f%%%s", azul, getChanceFalha() * 100, reset);
        } else {
            String simboloTriangulo = diffChanceFalha < 0 ? "▾" : "▴";
            chanceFalhaFormatada = String.format("%s%.0f%% %s(%s %.0f%%)%s", azul, getChanceFalha() * 100, cinza, simboloTriangulo, Math.abs(diffChanceFalha) * 100, reset);
        }

        // Energia da máquina. O estado físico (nova, desgastada, quebrada) fica no cabeçalho da máquina
        String statusEnergia = estaLigada() ? verde + "Ligada" + reset : cinza + "Desligada" + reset;

        // Custo operacional
        String custo = String.format("%sR$ %.2f%s", amarelo, getCustoOperacao(), reset);
        String capacidade = String.format("%s%d un/ciclo%s", azul, getCapacidadeMaxima(), reset);

        StringBuilder relatorio = new StringBuilder(String.format(
                """
                ├─ ⏻ Energia: %s
                ├─ ⚂ Chance de Falha: %s
                ├─ $ Custo Operacional: %s
                ├─ Capacidade Máx: %s
                └─ ⚙ Componentes
                """,
                statusEnergia,
                chanceFalhaFormatada,
                custo,
                capacidade
        ));

        // Cada parte se diagnostica
        List<String> componentes = new ArrayList<>();
        componentes.add(linhaComponente(!this.motor.estaQuebrado(), this.motor.diagnostico()));
        for (Sensor s : this.sensores) {
            componentes.add(linhaComponente(s.estaCalibrado(), s.diagnostico()));
        }
        componentes.addAll(diagnosticoPartesEspecificas());

        for (int i = 0; i < componentes.size(); i++) {
            String conector = (i == componentes.size() - 1) ? "└─" : "├─";
            relatorio.append("   ").append(conector).append(" ").append(componentes.get(i)).append("\n");
        }

        return relatorio.toString();
    }

    /**
     * Formata uma linha do diagnóstico de uma parte
     *
     * @param ok se a parte está funcionando
     * @param diagnostico texto da parte, ex.: "Sensor 1: OK (95%)"
     * @return linha colorida
     */
    protected String linhaComponente(boolean ok, String diagnostico) {
        return ConsolePrinter.colorir(ok ? ConsolePrinter.VERDE : ConsolePrinter.VERMELHO, diagnostico);
    }

    /**
     * Lista as partes c/ desgaste que vale mostrar ao uusário, p/ ele decidir se repara antes de a máquina
     * quebrar (partes defeituosas). Sensores que perderam só um pouco de calibração ficam de fora da lista,
     * mas ainda contam em {@link #temPartesDesgastadas()}.
     *
     * @return diagnóstico de cada parte desgastada, vazia se nada precisar de atenção
     */
    public List<String> listarPartesDesgastadas() {
        List<String> desgastadas = new ArrayList<>();
        if (!this.motor.estaNovo()) {
            desgastadas.add(this.motor.diagnostico());
        }
        for (Sensor s : this.sensores) {
            if (!s.estaCalibrado()) {
                desgastadas.add(s.diagnostico());
            }
        }
        desgastadas.addAll(diagnosticoPartesEspecificasDesgastadas());
        return desgastadas;
    }

    /**
     * Diz se alguma parte tem desgaste, por menor que seja. O reparo é por demanda: pode ser feito sempre
     * que isso for true, sem esperar a máquina quebrar
     *
     * @return true se alguma parte não estiver nova
     */
    public boolean temPartesDesgastadas() {
        boolean algumSensorUsado = this.sensores.stream().anyMatch(s -> !s.estaNovo());
        return !this.motor.estaNovo() || algumSensorUsado || !diagnosticoPartesEspecificasDesgastadas().isEmpty();
    }

    /**
     * Indica se a máquina precisa de reparo: motor abaixo do limiar crítico ou parte específica parada.
     * @return true se a máquina estiver quebrada.
     */
    @Override
    public boolean precisaManutencao() {
        return this.motor.estaQuebrado() || partesEspecificasQuebradas();
    }

    @Override
    public String gerarResumoProblema() {
        return String.format("%s (Saúde: %d/%d)", getNome(), getSaude(), getSaudeMaxima());
    }

    // === DELEGAÇÃO P/ AS PARTES ===

    /**
     * Saúde atual do motor da máquina
     *
     * @return saúde, de 0 até {@link #getSaudeMaxima()}
     */
    public int getSaude() {
        return this.motor.getSaude();
    }

    /**
     * Saúde de um motor novo ou recém-reparado
     *
     * @return saúde máxima
     */
    public int getSaudeMaxima() {
        return this.motor.getSaudeMaxima();
    }

    /**
     * Quantos sensores ainda estão calibrados
     *
     * @return número de sensores calibrados
     */
    public int contarSensoresCalibrados() {
        return (int) this.sensores.stream().filter(Sensor::estaCalibrado).count();
    }

    /**
     * Quantos sensores a máquina tem
     *
     * @return número de sensores
     */
    public int getQuantidadeSensores() {
        return this.sensores.size();
    }

    /**
     * Chance de falha atual, que piora c/ o desgaste do motor, c/ os sensores descalibrados, c/ a parte
     * específica do subtipo e c/ o cenário:
     * <pre>{@code
     * fracaoSensoresRuins = 1 - sensoresCalibrados / totalSensores
     * chanceFalha = chanceFalhaBase * fatorDesgaste * (1 + fracaoSensoresRuins) * multiplicadorCenario
     *             + chanceExtraPartesEspecificas
     * }</pre>
     * O fatorDesgaste vem do {@link Motor}: vale 1 c/ saúde cheia (não muda a chance) e chega perto de 2
     * quando o motor tá quase quebrando (quase dobra a chance). Sensores todos descalibrados também dobram.
     * A chance extra vem de {@link #chanceExtraPartesEspecificas()}
     *
     * @return chance de falha atual, entre 0 e 1
     */
    public double getChanceFalha() {
        double fracaoSensoresRuins = 1 - (double) contarSensoresCalibrados() / this.sensores.size();
        double chance = this.chanceFalhaBase * this.motor.fatorDesgaste() * (1 + fracaoSensoresRuins) * Maquina.multiplicadorCenario
                + chanceExtraPartesEspecificas();
        return Math.min(1.0, chance);
    }

    /**
     * Sorteia a falha c/ a chance atual de {@link #getChanceFalha()}
     *
     * @return true se a máquina falhou
     */
    protected boolean sorteiaFalha() {
        return GeradorAleatorio.chance(getChanceFalha());
    }

    // === GETTERS E LIGA/DESLIGA ===

    /** Desliga a máquina, o que faz {@link #processar(Produto)} parar de funcionar */
    public void desligar() {
        this.ligada = false;
    }

    /** Liga a máquina */
    public void ligar() {
        this.ligada = true;
    }

    /**
     * Diz se a máquina está ligada
     *
     * @return true se estiver ligada
     */
    public boolean estaLigada() {
        return ligada;
    }

    /**
     * Nome da máquina
     *
     * @return nome, ex.: "Máquina de Inspeção"
     */
    public String getNome() {
        return nome;
    }

    /**
     * Capacidade máxima de processamento da máquina.
     *
     * @return unidades por ciclo
     */
    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    /**
     * Chance base de falha, sem desgaste, sensores e cenário
     *
     * @return chance base, entre 0 e 1
     */
    public double getChanceFalhaBase() {
        return this.chanceFalhaBase;
    }

    /**
     * Chance de falha c/ o cenário aplicado mas c/ todas as partes novas (motor cheio, sensores calibrados e
     * parte específica sem desgaste). É a referência p/ medir quanto o desgaste das partes aumentou a chance
     *
     * @return chance base vezes o multiplicador do cenário, entre 0 e 1
     */
    public double getChanceFalhaCenario() {
        return Math.min(1.0, this.chanceFalhaBase * Maquina.multiplicadorCenario);
    }

    /**
     * Custo p/ a máquina processar uma unidade
     *
     * @return custo de operação, em R$
     */
    public double getCustoOperacao() {
        return custoOperacao;
    }

    /**
     * Multiplicador de falha do cenário atual, igual p/ todas as máquinas
     *
     * @return multiplicador do cenário
     */
    public static double getMultiplicadorCenario() {
        return multiplicadorCenario;
    }
}
