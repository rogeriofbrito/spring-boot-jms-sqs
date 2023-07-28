package dev.rogeriofbrito.springsqs.service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessProductRequest {

    private Integer id;
    private String title;
    private String brand;
}
