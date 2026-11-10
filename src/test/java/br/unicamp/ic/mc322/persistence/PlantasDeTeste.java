package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.model.demand.StatusDemanda;
import br.unicamp.ic.mc322.model.machine.MaquinaEmpacotamento;
import br.unicamp.ic.mc322.model.machine.MaquinaInspecao;
import br.unicamp.ic.mc322.model.machine.MaquinaProcessamento;
import br.unicamp.ic.mc322.model.productionline.LinhaDeProducao;
import br.unicamp.ic.mc322.model.rawmaterial.MateriaPrima;
import br.unicamp.ic.mc322.model.scenario.ConfiguracaoCenario;
import br.unicamp.ic.mc322.service.FabricaPlanta;
import br.unicamp.ic.mc322.service.Planta;
import br.unicamp.ic.mc322.strategy.CatalogoEstrategias;

import java.util.List;

/** Fábricas prontas p/ os testes de persistência */
final class PlantasDeTeste {
    private PlantasDeTeste() {
    }

    /**
     * Fábrica do cenário Apocalíptico em que nada está no valor padrão: partes desgastadas, quantidades de
     * partes diferentes do Menu, demandas em cada status, estoque quebrado e estratégia Ferrari
     */
    static Planta alterada() {
        ConfiguracaoCenario cenario = ConfiguracaoCenario.buscarPorTipo("Apocalíptico").orElseThrow();
        double mult = cenario.getMultiplicadorCenario();
        int desgaste = cenario.getDesgasteMaximo();

        MaquinaProcessamento processamento = new MaquinaProcessamento("Forno Velho", 18, 0.21, 0.77, mult, desgaste, 3, 4);
        processamento.restaurarEstado(72, List.of(1.0, 0.55, 0.9));
        processamento.restaurarResistencias(List.of(false, true, false, true));

        MaquinaEmpacotamento empacotamento = new MaquinaEmpacotamento("Embaladora", 19, 0.16, 0.51, mult, desgaste, 1);
        empacotamento.restaurarEstado(90, List.of(0.68));
        empacotamento.restaurarSeladora(0.3);

        MaquinaInspecao inspecao = new MaquinaInspecao("Olho de Lince", 17, 0.11, 0.44, mult, desgaste, 2, 2);
        inspecao.restaurarEstado(12, List.of(1.0, 0.75));
        inspecao.restaurarCameras(List.of(true, false));

        LinhaDeProducao linha = new LinhaDeProducao("Linha Teste");
        linha.adicionarMaquina(processamento);
        linha.adicionarMaquina(empacotamento);
        linha.adicionarMaquina(inspecao);

        List<Demanda> demandas = List.of(
                Demanda.restaurar(FabricaPlanta.buscarNoCatalogo("Copo").orElseThrow(), 30, 12, StatusDemanda.CANCELADA, 131.4, 5),
                Demanda.restaurar(FabricaPlanta.buscarNoCatalogo("Pote").orElseThrow(), 4, 4, StatusDemanda.CONCLUIDA, 52.25, 3),
                Demanda.restaurar(FabricaPlanta.buscarNoCatalogo("Kit Copo").orElseThrow(), 7, 0, StatusDemanda.PENDENTE, 0, 9)
        );

        return FabricaPlanta.montar(cenario, new MateriaPrima("Vidro", 37.5, "kg", 5, 1.25f), 412.35f, linha,
                demandas, CatalogoEstrategias.buscarPorNome("Ferrari").orElseThrow());
    }

    /** Fábrica nova do cenário Ideal */
    static Planta padrao() {
        return FabricaPlanta.criarPadrao(ConfiguracaoCenario.buscarPorTipo("Ideal").orElseThrow());
    }
}
