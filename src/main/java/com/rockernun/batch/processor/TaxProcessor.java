package com.rockernun.batch.processor;

import com.rockernun.common.Product;
import org.springframework.batch.item.ItemProcessor;

public class TaxProcessor implements ItemProcessor<Product, Product> {

    @Override
    public Product process(Product product) throws Exception {
        product.setPrice((int)(product.getPrice() * 1.1));
        return product;
    }
}
