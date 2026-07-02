package com.rockernun.batch.processor;

import com.rockernun.common.Product;
import org.springframework.batch.item.ItemProcessor;

public class PriceValidator implements ItemProcessor<Product, Product> {

    @Override
    public Product process(Product product) throws Exception {
        if (product.getPrice() <= 0) {
            System.out.println("필터링 되었습니다! " + product.getName());
            return null;
        }

        return product;
    }
}
