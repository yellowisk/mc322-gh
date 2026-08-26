package br.unicamp.ideal.domain.entities.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Product {
    private UUID id;
    private String name;
    private ProductStatus status;
    private int rawMaterialNeeded;

    public void process() {
        // TODO Check how to insert Exception here
        if (!status.equals(ProductStatus.READY))
            setStatus(ProductStatus.READY);
    }
}
