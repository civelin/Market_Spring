package com.market.dto.mapper;

import com.market.dto.request.ProductCreate;
import com.market.entity.productTypes.Product;
import com.market.utility.enums.ProductTypeEnum;
import com.market.repository.product.CosmeticRepository;
import com.market.repository.product.DrinkRepository;
import com.market.repository.product.FoodRepository;
import com.market.repository.product.SanitaryRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductToProductCreateMapper {
    private final FoodRepository foodRepository;
    private final DrinkRepository drinkRepository;
    private final CosmeticRepository cosmeticRepository;
    private final SanitaryRepository sanitaryRepository;

    public ProductToProductCreateMapper(FoodRepository foodRepository, DrinkRepository drinkRepository, CosmeticRepository cosmeticRepository, SanitaryRepository sanitaryRepository) {
        this.foodRepository = foodRepository;
        this.drinkRepository = drinkRepository;
        this.cosmeticRepository = cosmeticRepository;
        this.sanitaryRepository = sanitaryRepository;
    }

    public ProductCreate apply(Product product) {
        ProductCreate productCreate = new ProductCreate();
        ProductTypeEnum type = product.getType();

        switch (type.name()) {
            case "FOOD":
                productCreate.setWeight(foodRepository.findFoodByProductId(product.getId()).getWeight());
                break;
            case "DRINKS":
                productCreate.setVolume(drinkRepository.findDrinkByProductId(product.getId()).getVolume());
                break;
            case "COSMETICS":
                productCreate.setVolume(cosmeticRepository.findCosmeticByProductId(product.getId()).getVolume());
                productCreate.setWeight(cosmeticRepository.findCosmeticByProductId(product.getId()).getWeight());
                break;
            case "SANITARY":
                productCreate.setCount(sanitaryRepository.findSanitaryByProductId(product.getId()).getCount());
                break;

        }

        productCreate.setId(product.getId());
        productCreate.setType(product.getType());
        productCreate.setName(product.getName());
        productCreate.setAvailableQuantity(product.getAvailableQuantity());
        productCreate.setPriceBGN(product.getPriceBGN());
        productCreate.setImageUrl(product.getImageUrl());
        productCreate.setExpiredDate(product.getExpiredDate());

        return productCreate;

    }

}
