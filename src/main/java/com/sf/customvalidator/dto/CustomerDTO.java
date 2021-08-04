package com.sf.customvalidator.dto;


import com.sf.customvalidator.constant.CustomerType;
import com.sf.customvalidator.validator.CrossFieldValidator;
import com.sf.customvalidator.validator.SpElCrossFieldCondition;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@CrossFieldValidator(groups = {PostMapping.class}, conditions = {
        @SpElCrossFieldCondition(IF = "customerType == T(com.sf.customvalidator.constant.CustomerType).ORGANIZATION",
                THEN = "surname==null"),
        @SpElCrossFieldCondition(IF = "customerType == T(com.sf.customvalidator.constant.CustomerType).ORGANIZATION",
                THEN = "dob == null AND doi != null"),
        @SpElCrossFieldCondition(IF = "customerType == T(com.sf.customvalidator.constant.CustomerType).ORGANIZATION",
                THEN = "addresses!=null AND addresses.size() == 1"),
        @SpElCrossFieldCondition(IF = "customerType == T(com.sf.customvalidator.constant.CustomerType).PERSON",
                THEN = "surname!=null AND !surname.isEmpty()"),
        @SpElCrossFieldCondition(IF = "customerType == T(com.sf.customvalidator.constant.CustomerType).PERSON",
                THEN = "dob != null AND doi == null"),
        @SpElCrossFieldCondition(IF = "customerType == T(com.sf.customvalidator.constant.CustomerType).PERSON",
                THEN = "addresses!=null AND addresses.size() >= 1 AND addresses.size() <= 2")
})
public class CustomerDTO {
    @NotEmpty(groups = PostMapping.class)
    private String name;
    private String surname;
    private LocalDate dob;
    private LocalDate doi;
    private List<@Valid AddressDTO> addresses;
    @NotNull(groups = PostMapping.class)
    private CustomerType customerType;
}