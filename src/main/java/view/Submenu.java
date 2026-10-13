package view;

import domain.entities.productionmanager.GerenciadorProducao;

/**
 * Base de todo submenu do menu principal. Cada submenu tem um ícone, um nome e uma cor, e o {@link Menu}
 * monta a lista de opções a partir deles.
 *
 * @author Heitor Almeida
 */
public abstract class Submenu {
    /** Menu principal, p/ ler a entrada e mandar mensagens pro rodapé */
    protected final Menu menu;
    /** De onde o submenu lê e altera o estado da fábrica */
    protected final GerenciadorProducao gerenciadorProducao;
    /** Cor principal do submenu: ícone, ações, destaques e mensagens neutras */
    protected final String cor;

    /**
     * Guarda as dependências do submenu
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     * @param cor cor principal, uma das constantes do {@link ConsolePrinter}
     */
    protected Submenu(Menu menu, GerenciadorProducao gerenciadorProducao, String cor) {
        this.menu = menu;
        this.gerenciadorProducao = gerenciadorProducao;
        this.cor = cor;
    }

    /**
     * Ícone do submenu
     *
     * @return ícone já pintado c/ a cor do submenu
     */
    public abstract String icone();

    /**
     * Nome do submenu
     *
     * @return nome no menu principal, ex.: "Ver armazém"
     */
    public abstract String rotulo();

    /** Abre o submenu e fica no loop dele até o usuário escolher voltar */
    public abstract void exibir();

    /** Limpa a tela e imprime o cabeçalho c/ o ícone + nome em caixa alta */
    protected void imprimirCabecalho() {
        ConsolePrinter.limparTela();
        ConsolePrinter.cartao("%s", icone() + " " + rotulo().toUpperCase());
        System.out.println();
    }
}
