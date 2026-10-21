package br.unicamp.ic.mc322.model;

/**
 * Contrato p/ os componentes que aparecem na auditoria da fábrica: cada um informa o próprio estado
 * (relatório de diagnóstico), se precisa de manutenção e um resumo do problema.
 * Implementada por {@link br.unicamp.ic.mc322.model.machine.Maquina} e {@link br.unicamp.ic.mc322.model.product.Produto}
 *
 * @author Heitor Almeida
 */
public interface Auditavel {
    /**
     * Estado detalhado do componente
     *
     * @return diagnóstico formatado
     */
    String gerarRelatorioDiagnostico();

    /**
     * Uma linha identificando o componente e o motivo do alerta, para a lista de problemas da auditoria
     *
     * @return resumo do problema, ex.: "[Máquina] Máquina de Inspeção (Saúde: 10/100)"
     */
    String gerarResumoProblema();

    /**
     * Diz se o componente precisa de atenção e deve aparecer na lista de problemas da auditoria
     *
     * @return true se precisar de manutenção
     */
    boolean precisaManutencao();
}
