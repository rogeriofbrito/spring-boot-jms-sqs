package dev.rogeriofbrito.springbootjmssqs.service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessProductResponse {

    private boolean success;
}
