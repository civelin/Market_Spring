package com.market.util.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DrinksValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateDrinks {
    String message() default "моля попълнете полето";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
