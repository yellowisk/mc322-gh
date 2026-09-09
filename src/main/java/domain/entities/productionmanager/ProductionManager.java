package domain.entities.productionmanager;

import domain.entities.demand.Demand;
import domain.entities.machines.machine.Machine;
import domain.entities.product.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductionManager {
    private List<Demand> demands = new ArrayList<>();
    private List<Product> fabricatedProducts = new ArrayList<>();
    private List<Machine> machines = new ArrayList<>();
    private Product chosenProduct;

    public List<Demand> getDemands() { return demands; }

    public void fabricateDemand(Demand demand) {
        System.out.println("Produto fabricado com sucesso.");
    }

    public void registerDemand(Product produto, int quantity) {
        demands.add(new Demand(produto.getName(), quantity));
    }
}

