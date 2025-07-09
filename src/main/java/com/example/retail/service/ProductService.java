package com.example.retail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.retail.repository.ProductRepository;

@Service
public class ProductService {

    private final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    public void callDBService() {
        productRepository.executeSleep();
    }

}
