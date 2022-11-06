package com.github.rogeriofbrito.springsqs.service;

import com.github.rogeriofbrito.springsqs.service.model.ProcessProductRequest;
import com.github.rogeriofbrito.springsqs.service.model.ProcessProductResponse;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    public ProcessProductResponse processProduct(ProcessProductRequest processProductRequest) {
        return ProcessProductResponse.builder().success(true).build();
    }
}
