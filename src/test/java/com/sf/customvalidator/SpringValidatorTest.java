package com.sf.customvalidator;

import com.sf.customvalidator.example.constant.Country;
import com.sf.customvalidator.example.constant.CustomerType;
import com.sf.customvalidator.example.dto.AddressDTO;
import com.sf.customvalidator.example.dto.CustomerDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Collections;
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
        Set<ConstraintViolation<CustomerDTO>> constraintViolations = validator.validate(CustomerDTO.builder().customerType(CustomerType.ORGANIZATION).
                name("ABC LLP").doi(LocalDate.of(2020, 01, 01))
                .addresses(Collections.singletonList(AddressDTO.builder()
                        .houseNo("6/7").lane("lane").areaCode("22222")
                        .country(Country.US).state("NY").build())).build(), PostMapping.class);
        Assert.assertEquals(constraintViolations.size(), 0);
    }

    @Test
    public void testFailureOnAddressAreaCode() {
        Set<ConstraintViolation<CustomerDTO>> constraintViolations = validator.validate(CustomerDTO.builder().customerType(CustomerType.ORGANIZATION).
                name("ABC LLP").doi(LocalDate.of(2020, 01, 01))
                .addresses(Collections.singletonList(AddressDTO.builder()
                        .houseNo("6/7").lane("lane").areaCode("222222")
                        .country(Country.US).state("NY").build())).build(), PostMapping.class);
        Assert.assertEquals(constraintViolations.size(), 2);
    }
}
