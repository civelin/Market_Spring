package com.market.service;

import com.market.dto.ProductCreate;
import org.springframework.stereotype.Service;


public interface FoodService{
    void save(ProductCreate productCreate);
}
