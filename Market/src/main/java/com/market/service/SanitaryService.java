package com.market.service;

import com.market.dto.ProductCreate;
import org.springframework.stereotype.Service;


public interface SanitaryService {
    void save(ProductCreate productCreate);
}
