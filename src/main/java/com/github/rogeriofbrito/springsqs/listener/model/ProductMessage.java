package com.github.rogeriofbrito.springsqs.listener.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductMessage {

    private Integer id;
    private String title;
    private String brand;
}
