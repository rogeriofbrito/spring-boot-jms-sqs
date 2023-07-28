package dev.rogeriofbrito.springsqs.service;

import dev.rogeriofbrito.springsqs.service.model.ProcessProductRequest;
import dev.rogeriofbrito.springsqs.service.model.ProcessProductResponse;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    public ProcessProductResponse processProduct(ProcessProductRequest processProductRequest) {
        return ProcessProductResponse.builder().success(true).build();
    }
}
