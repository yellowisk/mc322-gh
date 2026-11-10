package br.unicamp.ic.mc322.view;

import br.unicamp.ic.mc322.exception.OpcaoInvalidaException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.persistence.PersistenciaPlanta;
import br.unicamp.ic.mc322.service.GerenciadorProducao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Submenu de persistência. Permite salvar o estado atual da fábrica em um arquivo XML
 * ou carregar um estado previamente salvo, com tratamento de confirmação.
 *
 * @author Glayson Oliveira
 */
public class MenuPersistencia extends Submenu {

    /**
     * Cria o submenu de persistência com a cor roxa.
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção (da fábrica atual)
     */
    public MenuPersistencia(Menu menu, GerenciadorProducao gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.ROXO);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "💾");
    }

    @Override
    public String rotulo() {
        return "Salvar e carregar";
    }

    /**
     * Loop do submenu. Mostra o status atual do save, pede opções para salvar ou carregar.
     * Após um carregamento bem-sucedido, o submenu é encerrado voltando ao menu principal.
     */
    @Override
    public void exibir() {
        PersistenciaPlanta persistencia = menu.getPersistencia();
        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        while (true) {
            imprimirCabecalho();

            // Informações do arquivo de save
            System.out.println(" Arquivo de save: " + ConsolePrinter.colorir(ConsolePrinter.CINZA, persistencia.getArquivo().toString()));

            Optional<LocalDateTime> dataSave = persistencia.dataUltimoSave();
            if (dataSave.isPresent()) {
                System.out.println(" Último save em:  " + ConsolePrinter.colorir(ConsolePrinter.AZUL, dataSave.get().format(formatadorData)));
            } else {
                System.out.println(ConsolePrinter.colorir(ConsolePrinter.CINZA, " Nenhum save encontrado ainda."));
            }
            System.out.println();

            ConsolePrinter.listaOpcoes("Salvar estado da fábrica", "Carregar estado salvo", "Voltar");

            menu.imprimirRodape();

            int opcao = menu.lerInteiro("Escolha uma opção de " + ConsolePrinter.colorir(cor, "PERSISTÊNCIA") + ": ");

            if (opcao == 0) break;

            try {
                Menu.validarOpcao(opcao, 2, "Dessa vez não é! Opção inválida!");

                if (opcao == 1) {
                    // Sobrescreve o arquivo salvando a planta atual
                    persistencia.salvar(menu.getPlanta());
                    menu.setUltimaMensagem(ConsolePrinter.textoOk("Fábrica salva com sucesso em %s!", persistencia.getArquivo().getFileName()));
                }
                else if (opcao == 2) {
                    if (!persistencia.existeSave()) {
                        menu.setUltimaMensagem(ConsolePrinter.textoFalha("Dessa vez não é! Não há arquivo de save para carregar."));
                        continue;
                    }

                    // Confirmação
                    System.out.println("\n" + ConsolePrinter.colorir(ConsolePrinter.AMARELO, " ATENÇÃO: O estado atual não salvo será perdido."));
                    int confirma = menu.lerInteiro(" Tem certeza que deseja carregar? (1. Sim / 2. Não): ");

                    if (confirma == 1) {
                        menu.substituirPlanta(persistencia.carregar());
                        menu.setUltimaMensagem(ConsolePrinter.textoOk("Fábrica carregada com sucesso!"));
                        return;
                    } else {
                        menu.setUltimaMensagem(ConsolePrinter.colorir(ConsolePrinter.CINZA, "ℹ Carregamento cancelado. Estado anterior mantido."));
                    }
                }
            } catch (OpcaoInvalidaException | PersistenciaException e) {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("%s", e.getMessage()));
            }
        }
    }
}