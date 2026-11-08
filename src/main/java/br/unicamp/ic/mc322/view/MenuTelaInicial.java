package br.unicamp.ic.mc322.view;

import br.unicamp.ic.mc322.exception.OpcaoInvalidaException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.persistence.PersistenciaPlanta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Tela inicial
 * Apresenta o banner do jogo e as opções para iniciar uma nova fábrica ou carregar um save.
 */
public class MenuTelaInicial {

    private final Menu menu;
    private String ultimaMensagem;

    /**
     * Construtor.
     * @param menu O menu principal
     */
    public MenuTelaInicial(Menu menu) {
        this.menu = menu;
    }

    /**
     * Exibe a tela inicial e trata a opção escolhida.
     *
     * @return 1 se o usuário escolheu "Nova Planta", 2 se carregou um save com sucesso, ou 0 para Sair.
     */
    public int exibir() {
        PersistenciaPlanta persistencia = menu.getPersistencia();
        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


        while (true) {
            imprimirLogo();

            if (ultimaMensagem != null) {
                ConsolePrinter.cartao("%s", ultimaMensagem);
                ultimaMensagem = null;
            } else {
                System.out.println();
            }

            Optional<LocalDateTime> dataSave = persistencia.dataUltimoSave();
            String textoCarregar = String.format("(Último save em: %s)",
                    ConsolePrinter.colorir(ConsolePrinter.AZUL, dataSave.get().format(formatadorData)));


            ConsolePrinter.listaOpcoes("Nova Planta",
                    String.format("Carregar Planta Salva %s",textoCarregar),
                    "Sair");

            ConsolePrinter.linha();
            System.out.println();

            int opcao = menu.lerInteiro("Escolha: ");

            if (opcao == 0) {
                return 0; // Sair
            }

            try {
                Menu.validarOpcao(opcao, 2, "Dessa vez não é! Opção inválida!");

                if (opcao == 1) {
                    return 1; // Nova Planta
                }
                else if (opcao == 2) {
                    if (!persistencia.existeSave()) {
                        ultimaMensagem = ConsolePrinter.textoFalha("Dessa vez não é! Não há arquivo de save para carregar.");
                        continue;
                    }

                    // Tenta carregar o save imediatamente
                    menu.substituirPlanta(persistencia.carregar());
                    menu.setUltimaMensagem(ConsolePrinter.textoOk("Fábrica carregada com sucesso!"));
                    return 2; // Save carregado
                }
            } catch (OpcaoInvalidaException | PersistenciaException e) {
                ultimaMensagem = ConsolePrinter.textoFalha("%s", e.getMessage());
            }
        }
    }

    /**
     * Imprime o banner de boas-vindas do jogo transferido da classe Menu
     */
    private void imprimirLogo() {
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
    }
}