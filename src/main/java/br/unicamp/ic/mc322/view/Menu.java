package br.unicamp.ic.mc322.view;

import br.unicamp.ic.mc322.exception.OpcaoInvalidaException;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.model.scenario.ConfiguracaoCenario;
import br.unicamp.ic.mc322.persistence.PersistenciaPlanta;
import br.unicamp.ic.mc322.service.FabricaPlanta;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.service.Planta;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principal para os menus. Aqui fica a lógica principal
 * das telas da fábrica e é a partir daqui que os submenus são chamados
 *
 * @author Heitor Almeida
 * @author Glayson Oliveira
 */
public class Menu {
    private Planta planta; // fábrica em uso: cenário, gerenciador e linha
    private final PersistenciaPlanta persistencia = new PersistenciaPlanta(); // save em dados/planta.xml

    private final Scanner leitor = new Scanner(System.in);
    private String ultimaMensagem; // last mensagem, shows up in the next footer
    private List<Submenu> submenus;

    /** Cria o menu. O cenário e o gerenciador da produção só são montados em {@link #iniciar()} */
    public Menu() {
    }


    /**
     * Método de inicialização do menu, chamado na main
     */
    public void iniciar() {
        boolean rodando = true;

        imprimirTelaInicial();
        ConfiguracaoCenario cenario = escolherCenario();
        if (cenario == null) {
            return;
        }

        substituirPlanta(FabricaPlanta.criarPadrao(cenario));

        // Menu: lista de submenus
        List<String> listaOpcoes = new ArrayList<>(
                submenus.stream()
                    .map(submenu -> submenu.icone() + " " + submenu.rotulo())
                    .toList()
        );
        listaOpcoes.add("Sair");
        String[] opcoes = listaOpcoes.toArray(new String[0]);

        /**
         * Lppp principal
         */
        while (rodando) {
            ConsolePrinter.limparTela();
            ConsolePrinter.cartao(ConsolePrinter.colorir(ConsolePrinter.CINZA, "⌂") + " FÁBRICA IDEAL");
            System.out.println();
            System.out.println(" Cenário atual:    " + planta.getCenario().getNomeExibicao());
            System.out.println(" Estratégia atual: " + planta.getGerenciador().getEstrategiaAtual().getNomeEstrategia() + "\n");

            ConsolePrinter.listaOpcoes(opcoes);

            imprimirRodape();
            int opcao = lerInteiro("Escolha: ");

            if (opcao == 0) break;

            try {
                validarOpcao(opcao, submenus.size(), "Dessa vez não é! Opção inválida!");
                submenus.get(opcao - 1).exibir();
            } catch (PlantaException e) {
                // Qualquer erro de domínio que um submenu não tratou volta pro menu principal
                setUltimaMensagem(ConsolePrinter.textoFalha("%s", e.getMessage()));
            }
        }
    }

    /**
     * Passa a usar outra fábrica, como uma carregada do save. A fábrica anterior é descartada inteira e
     * os submenus são recriados p/ trazer o gerenciador da fábrica.<br>
     *
     * @param nova fábrica montada e validada
     */
    void substituirPlanta(Planta nova) {
        nova.ativar();
        this.planta = nova;
        montarSubmenus();
    }

    /**
     * Fábrica em uso
     *
     * @return planta atual
     */
    Planta getPlanta() {
        return planta;
    }

    /**
     * Onde a fábrica é salva e de onde ela é carregada
     *
     * @return persistência do save
     */
    PersistenciaPlanta getPersistencia() {
        return persistencia;
    }

    /**
     * Cria os submenus apontando p/ a fábrica em uso. A ordem da lista é o número da opção no menu principal
     */
    private void montarSubmenus() {
        GerenciadorProducao gerenciador = planta.getGerenciador();
        submenus = List.of(
                new MenuAtualizarDemandas(this, gerenciador),
                new MenuFabricarDemandas(this, gerenciador),
                new MenuArmazem(this, gerenciador),
                new MenuComprarMateriaPrima(this, gerenciador),
                new MenuEstrategia(this, gerenciador),
                new MenuLinha(this, gerenciador, planta.getLinha()),
                new MenuAuditoria(this, gerenciador, planta.getLinha()),
                new MenuArmazemMateriaPrima(this, gerenciador),
                new MenuVelocidade(this, gerenciador)
        );
    }

    /**
     * Tela inicial
     */
    private void imprimirTelaInicial() {
        ConsolePrinter.limparTela();
        ConsolePrinter.linha();
        System.out.println(ConsolePrinter.NEGRITO + ConsolePrinter.LARANJA
                + ConsolePrinter.centralizar(65, "FÁBRICA IDEAL") + ConsolePrinter.RESET);
        System.out.println(ConsolePrinter.CINZA
                + ConsolePrinter.centralizar(65, "\"E, sim, cliente ganhou um balão de presente! :D\"")
                + ConsolePrinter.RESET);
        ConsolePrinter.linha();
        System.out.println();
        System.out.println("   Bem-vindos à " + ConsolePrinter.LARANJA + "Fábrica Ideal" + ConsolePrinter.RESET + "! Aqui cliente não pediu");
        System.out.println("   150 kg de vidro e, sim, ganha produtos ótimos de presente!");
        System.out.println("   Os balões de presente ficam no " + ConsolePrinter.AZUL + "@merc.adinhoideall" + ConsolePrinter.RESET + ", nosso");
        System.out.println("   fiel cliente!");
        System.out.println();
        System.out.println("   " + ConsolePrinter.VERDE + "Desenvolvido por:" + ConsolePrinter.RESET);
        System.out.println("     • Heitor Almeida    " + ConsolePrinter.CINZA + "RA: 245293" + ConsolePrinter.RESET);
        System.out.println("     • Glayson Oliveira  " + ConsolePrinter.CINZA + "RA: 281213" + ConsolePrinter.RESET);
        System.out.println();
        ConsolePrinter.linha();
        System.out.println();
        System.out.println(ConsolePrinter.colorir(ConsolePrinter.AMARELO, "Pressione ENTER para começar..."));
        lerLinha();
    }

    /**
     * Tela de seleção de cenário
     *
     * @return cenário escolhido, ou null se o usuário escolheu sair
     */
    private ConfiguracaoCenario escolherCenario() {

        List<ConfiguracaoCenario> cenarios = ConfiguracaoCenario.CENARIOS;

        // Lista de strings para o ConsolePrinter.listaOpcoes
        List<String> nomes = new ArrayList<>(cenarios.stream().map(ConfiguracaoCenario::getNomeExibicao).toList());
        nomes.add("Sair");
        String[] nomesCenarios = nomes.toArray(new String[0]);

        while (true) {
            ConsolePrinter.limparTela();
            ConsolePrinter.cartao("SELECIONADOR DE CENÁRIO");

            ConsolePrinter.listaOpcoes(nomesCenarios);

            int opcao = lerInteiro("Escolha: ");

            if (opcao == 0) return null;

            try {
                validarOpcao(opcao, cenarios.size(), "Dessa vez não é! Opção de cenário inválida!");
                return cenarios.get(opcao - 1);
            } catch (OpcaoInvalidaException e) {
                System.out.println("\n" + ConsolePrinter.textoFalha("%s", e.getMessage()));
                esperarEnter();
            }
        }
    }

    // === Stuff the submenus use ===

    /**
     * Confere se a opção escolhida existe numa lista numerada de 1 até {@code quantidade}
     *
     * @param opcao opção digitada pelo usuário
     * @param quantidade quantas opções a lista tem
     * @param mensagem mensagem de erro p/ mostrar se a opção não existir
     * @throws OpcaoInvalidaException se a opção estiver fora da lista
     */
    static void validarOpcao(int opcao, int quantidade, String mensagem) {
        if (opcao < 1 || opcao > quantidade) {
            throw new OpcaoInvalidaException(mensagem);
        }
    }

    /**
     * Lê um inteiro do usuário. Se ele digitar algo que não é número, avisa e pergunta de novo.
     * Se a entrada do terminal acabar (ex.: Ctrl+D), retorna 0, que em todo menu é "voltar/sair",
     * então o programa vai saindo dos menus e termina normalmente
     *
     * @param texto texto mostrado antes da entrada
     * @return o número digitado, ou 0 se a entrada acabou
     */
    int lerInteiro(String texto) {
        while (true) {
            System.out.print(texto);
            if (!leitor.hasNext()) {
                return 0;
            }
            if (leitor.hasNextInt()) {
                return leitor.nextInt();
            }
            leitor.next();
            System.out.println(ConsolePrinter.textoFalha("Dessa vez não é! Digite um número."));
        }
    }

    /** Espera o ENTER, mas antes consome a linha que sobra depois do nextInt() */
    void esperarEnter() {
        System.out.println("\n" + ConsolePrinter.colorir(ConsolePrinter.AMARELO, "Pressione ENTER para voltar..."));
        lerLinha();
        lerLinha();
    }

    /** Lê uma linha inteira da entrada. Se a entrada já acabou, não faz nada */
    private void lerLinha() {
        if (leitor.hasNextLine()) {
            leitor.nextLine();
        }
    }

    /**
     * Guarda uma mensagem p/ aparecer no próximo rodapé, ex.: o resultado de uma operação
     *
     * @param mensagem mensagem já formatada, normalmente vinda de {@link ConsolePrinter#textoOk} ou {@link ConsolePrinter#textoFalha}
     */
    void setUltimaMensagem(String mensagem) {
        this.ultimaMensagem = mensagem;
    }

    /**
     * Imprime o rodapé dos menus: a última mensagem guardada (se tiver, e depois apaga ela) e o resumo
     * c/ verba, matéria-prima e produtos no armazém
     */
    void imprimirRodape() {
        if (this.ultimaMensagem != null) {
            ConsolePrinter.cartao("%s", this.ultimaMensagem);
            this.ultimaMensagem = null;
        } else {
            ConsolePrinter.linha();
        }
        GerenciadorProducao gerenciador = planta.getGerenciador();
        ConsolePrinter.imprimirResumo(gerenciador.getVerba(),
                gerenciador.getMateriaPrima().getQuantidade(),
                gerenciador.getProdutosFabricados().size());
        System.out.println();
    }
}
