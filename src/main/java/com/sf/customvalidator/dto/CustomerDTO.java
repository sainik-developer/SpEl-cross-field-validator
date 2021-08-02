package com.sf.customvalidator.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.sf.customvalidator.constant.CustomerType;
import com.sf.customvalidator.validator.CrossFieldValidator;
import com.sf.customvalidator.validator.SpElCrossFieldCondition;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

@Data
@CrossFieldValidator(groups = {PostMapping.class, PutMapping.class}, conditions = {
        @SpElCrossFieldCondition(IF = "customerType == T(com.sf.customvalidator.constant.CustomerType).ORGANIZATION", THEN = "surname==null"),
        @SpElCrossFieldCondition(IF = "customerType == T(com.sf.customvalidator.constant.CustomerType).ORGANIZATION", THEN = "surname!=null AND !surname.isEmpty()")
})
public class CustomerDTO {
    @NotEmpty(groups = PostMapping.class)
    private String name;
    @NotEmpty(groups = PostMapping.class)
    private String surname;
    @NotEmpty(groups = PostMapping.class)
    private LocalDate dob;
    private List<@Valid AddressDTO> addresses;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    @NotEmpty(groups = PostMapping.class)
    private CustomerType customerType;
}

