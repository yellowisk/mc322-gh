package br.unicamp.ideal;

import br.unicamp.ideal.domain.entities.RawMaterial;
import br.unicamp.ideal.domain.entities.product.Product;
import br.unicamp.ideal.domain.entities.product.ProductStatus;

import java.util.UUID;

public class Main {
    static void main() {
        RawMaterial rawMaterial = new RawMaterial("Vidro", 10, "g", 5);
        Product product = new Product(UUID.randomUUID(), "Copo", ProductStatus.READY, 6);
        System.out.println(product);
        System.out.println(rawMaterial);
    }
}
