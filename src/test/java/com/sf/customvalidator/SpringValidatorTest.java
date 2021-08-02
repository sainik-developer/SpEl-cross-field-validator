package com.sf.customvalidator;

import com.sf.customvalidator.dto.CustomerDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@SpringBootTest
public class SpringValidatorTest {

    private Validator validator;

    @Before
    public void Before() {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void testSuccess() {
        final CustomerDTO customerDTO = new CustomerDTO();
        Set<ConstraintViolation<CustomerDTO>> constraintViolations = validator.validate(customerDTO, PostMapping.class);
        Assert.assertEquals(constraintViolations.size(), 0);
    }
}
