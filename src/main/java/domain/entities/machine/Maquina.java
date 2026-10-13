package domain.entities.machine;

import domain.entities.product.Produto;
import domain.exceptions.MachineNeedsRepairException;
import domain.exceptions.MachineUnavailableException;
import domain.interfaces.Auditavel;
import domain.utils.GeradorAleatorio;
import view.ConsolePrinter;

/**
 * Máquina de uma etapa da linha de produção. Cada {@link #processar(Produto)} desgasta a máquina,
 * e quanto menor a saúde, maior a chance de falha. Abaixo do limiar crítico ela quebra e precisa de reparo
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
    private StatusDeMaquina status = StatusDeMaquina.FUNCIONAL;
    /** Saúde máxima. */
    private static final int saudeMaxima = 100;
    private int saude = saudeMaxima;
    /** Limiar crítico da saúde para manutenção. */
    private final int saudeCritica = 15;
    private final int desgasteMaximo;

    /**
     * Cria a máquina desligada e c/ saúde máxima
     *
     * @param nome nome que aparece nos menus, ex.: "Máquina de Inspeção"
     * @param capacidadeMaxima unidades por ciclo
     * @param chanceFalhaBase chance base de falha, entre 0 e 1
     * @param custoOperacao custo p/ processar uma unidade, em R$
     * @param multiplicadorCenario multiplicador de falha do cenário. É static, então vale p/ todas as máquinas
     * @param desgasteMaximo desgaste máximo por uso, cada uso tira de 1 a esse valor da saúde
     */
    public Maquina(String nome, int capacidadeMaxima, double chanceFalhaBase,
                   double custoOperacao, double multiplicadorCenario,
                   int desgasteMaximo) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.chanceFalhaBase = chanceFalhaBase;
        this.custoOperacao = custoOperacao;
        Maquina.multiplicadorCenario = multiplicadorCenario;
        this.desgasteMaximo = desgasteMaximo;
    }

    /**
     * Método auxiliar do método 'processar()'. Esse método será
     * sobrescrito pelas subclasses de {@link Maquina}.
     *
     * @param produto O produto deverá ser fabricado.
     * @return Produto fabricado.
     */
    public abstract Produto processarEtapa(Produto produto);

    /**
     * Confere se a máquina pode processar agora, sem mexer em nada. A produção chama isso antes de cobrar
     * a etapa, p/ não pagar por uma operação que não vai acontecer
     *
     * @throws MachineUnavailableException se a máquina estiver desligada
     * @throws MachineNeedsRepairException se a saúde estiver abaixo do limiar crítico
     */
    public void verificarDisponivel() {
        if (!estaLigada()) {
            String mensagem = String.format("Eitcha, João! A %s não pode processar nada, ela tá desligada!", getNome().toLowerCase());
            throw new MachineUnavailableException(mensagem);
        }

        if (precisaManutencao()) {
            String mensagem = String.format("A %s quebrou!", getNome().toLowerCase());
            throw new MachineNeedsRepairException(mensagem);
        }
    }

    /**
     * Passa o produto pela etapa da máquina via {@link #processarEtapa(Produto)} e desgasta a máquina.
     * As checagens e o desgaste ficam aqui, a etapa ({@link #processarEtapa(Produto)}) em si fica em cada subclasse
     *
     * @param produto modelo do catálogo ou unidade vinda da etapa anterior
     * @return o produto depois da etapa
     * @throws MachineUnavailableException se a máquina estiver desligada
     * @throws MachineNeedsRepairException se a saúde estiver abaixo do limiar crítico
     */
    public Produto processar(Produto produto) {
        verificarDisponivel();

        Produto resultado = processarEtapa(produto);
        desgastar(); // Trecho que será rodado sempre que o 'processar()' for utilizado
        return resultado;
    }

    /**
     * Tipo da máquina, usado nos logs da produção
     *
     * @return tipo, ex.: "Empacotamento"
     */
    public abstract String getTipo();

    /* ====== Métodos concretos ======*/

    /**
     * Sorteia se a máquina falhou nessa unidade, c/ a chance de {@link #getChanceFalha()}.
     * Se falhou, o produto ganha 0.12 * multiplicador do cenário de risco acumulado, que pesa na inspeção
     *
     * @param produto produto que pode sair c/ defeito
     */
    protected void tentaAumentarRisco(Produto produto) {
        if (sorteiaFalha()) {
            double incremento = 0.12 * multiplicadorCenario;
            produto.aumentarProbabilidadeFalha(incremento);
        }
    }

    // === SAÚDE DA MÁQUINA ===

    /**
     * Diminui a saúde da máquina em um valor sorteado em [1, desgasteMaximo], que vem do cenário.
     * Se a saúde cair abaixo de saudeCritica, a máquina fica {@link StatusDeMaquina#QUEBRADA}
     */
    private void desgastar() {
        this.saude -= GeradorAleatorio.sortearInt(this.desgasteMaximo) + 1;
        if (this.saude < this.saudeCritica) {
            this.status = StatusDeMaquina.QUEBRADA;
        }
    }

    /**
     * Restaura a saúde pra saudeMaxima e volta o status pra {@link StatusDeMaquina#FUNCIONAL}.
     */
    public void reparar() {
        setSaude(saudeMaxima);
        this.status = StatusDeMaquina.FUNCIONAL;
    }

    // === AUDITÁVEL ===

    /**
     * Retorna um relatório de diagnóstico formatado com o estado completo
     * da máquina, incluindo saúde, status, custos, etc.
     * @return String formatada com o diagnóstico.
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        String reset = ConsolePrinter.RESET;
        String azul = ConsolePrinter.AZUL;
        String verde = ConsolePrinter.VERDE;
        String vermelho = ConsolePrinter.VERMELHO;
        String amarelo = ConsolePrinter.AMARELO;
        String cinza = ConsolePrinter.CINZA;

        // Saúde
        String corSaude = precisaManutencao() ? vermelho : verde;
        String saudeFormatada = String.format("%s%d/%d%s", corSaude, getSaude(), getSaudeMaxima(), reset);

        // Chance de falha
        String chanceFalhaFormatada;
        if (getChanceFalhaBase() == getChanceFalha()) {
            chanceFalhaFormatada = String.format("%s %.2f%s", azul, getChanceFalha(), reset);
        } else {
            double diffChanceFalha = getChanceFalha() - getChanceFalhaBase();
            String simboloTriangulo = diffChanceFalha < 0 ? "▾" : "▴";
            chanceFalhaFormatada = String.format("%s⚂ %.0f%% %s(%s %.0f%%)%s", azul, getChanceFalha() * 100, cinza, simboloTriangulo, diffChanceFalha * 100, reset);
        }

        // Status
        String statusEnergia = estaLigada() ? verde + "Ligada" + reset : cinza + "Desligada" + reset;
        String statusFisico = (this.status == StatusDeMaquina.FUNCIONAL) ? verde + "✓ Funcional" + reset : vermelho + "✗ Quebrada" + reset;

        // Custo operacional
        String custo = String.format("%sR$ %.2f%s", amarelo, getCustoOperacao(), reset);
        String capacidade = String.format("%s%d un/ciclo%s", azul, getCapacidadeMaxima(), reset);

        // Texto
        return String.format(
                """
                        ├─ ⏻ Status: %s | %s
                        ├─ ❤ Saúde: %s
                        ├─ ⚂ Chance de Falha: %s
                        ├─ $ Custo Operacional: %s
                        └─ Capacidade Máx: %s
                """,
                statusEnergia, statusFisico,
                saudeFormatada,
                chanceFalhaFormatada,
                custo,
                capacidade
        );
    }

    /**
     * Indica se a saúde da máquina está abaixo do limiar crítico.
     * @return true se a saúde estiver abaixo do limiar crítico.
     */
    @Override
    public boolean precisaManutencao() {
        return this.saude < this.saudeCritica;
    }

    @Override
    public String gerarResumoProblema() {
        return String.format("[Máquina] %s (Saúde: %d/%d)", getNome(), getSaude(), getSaudeMaxima());
    }

    /**
     * Saúde atual da máquina
     *
     * @return saúde, de 0 até {@link #getSaudeMaxima()}
     */
    public int getSaude() {
        return saude;
    }

    /**
     * Sobrescreve a saúde direto, sem mexer no status
     *
     * @param saude nova saúde
     */
    public void setSaude(int saude) {
        this.saude = saude;
    }

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
     * Chance de falha atual, que piora c/ o desgaste e c/ o cenário:
     * <pre>{@code
     * fatorDesgaste = 1 + (saudeMaxima - saude) / saudeMaxima
     * chanceFalha = chanceFalhaBase * fatorDesgaste * multiplicadorCenario
     * }</pre>
     * O fatorDesgaste vale 1 c/ saúde cheia (não muda a chance) e chega perto de 2 quando a máquina
     * tá quase quebrando (quase dobra a chance)
     *
     * @return chance de falha atual, entre 0 e 1
     */
    public double getChanceFalha() {
        double fatorDesgaste = 1 + (double) (this.getSaudeMaxima() - this.saude) / this.getSaudeMaxima();
        return this.chanceFalhaBase * fatorDesgaste * Maquina.multiplicadorCenario;
    }

    /**
     * Chance base de falha, sem saúde e sem cenário
     *
     * @return chance base, entre 0 e 1
     */
    public double getChanceFalhaBase() {
        return this.chanceFalhaBase;
    }

    /**
     * Sorteia a falha c/ a chance atual de {@link #getChanceFalha()}
     *
     * @return true se a máquina falhou
     */
    protected boolean sorteiaFalha() {
        return GeradorAleatorio.chance(getChanceFalha());
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
     * Saúde de uma máquina nova ou recém-reparada
     *
     * @return saúde máxima
     */
    public int getSaudeMaxima() {
        return saudeMaxima;
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
