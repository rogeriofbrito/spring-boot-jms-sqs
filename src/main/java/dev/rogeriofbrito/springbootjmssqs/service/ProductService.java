package dev.rogeriofbrito.springbootjmssqs.service;

import dev.rogeriofbrito.springbootjmssqs.service.model.ProcessProductRequest;
import dev.rogeriofbrito.springbootjmssqs.service.model.ProcessProductResponse;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    public ProcessProductResponse processProduct(ProcessProductRequest processProductRequest) {
        return ProcessProductResponse.builder().success(true).build();
    }
}
