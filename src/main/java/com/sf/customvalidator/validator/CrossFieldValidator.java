package com.sf.customvalidator.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { CrossFieldValidatorImpl.class })
@Documented
public @interface CrossFieldValidator {
    String message() default "failed!";

    Class<?>[] groups() default { };

    Class<? extends Payload>[]payload() default {};

    SpElCrossFieldCondition[] conditions();
}
