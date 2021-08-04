package com.sf.customvalidator.dto;

import com.sf.customvalidator.constant.Country;
import com.sf.customvalidator.validator.CrossFieldValidator;
import com.sf.customvalidator.validator.SpElCrossFieldCondition;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Data
@Builder
@CrossFieldValidator(groups = {PostMapping.class}, conditions = {
        @SpElCrossFieldCondition(IF = "country == T(com.sf.customvalidator.constant.Country).US OR country == T(com.sf.customvalidator.constant.Country).DE", THEN = "areaCode != null && areaCode.length() == 5"),
        @SpElCrossFieldCondition(IF = "country == T(com.sf.customvalidator.constant.Country).IND", THEN = "areaCode != null && areaCode.length() == 6")
})
public class AddressDTO {
    @NotEmpty(groups = {PostMapping.class})
    private String houseNo;
    @NotEmpty(groups = {PostMapping.class})
    private String lane;
    @NotNull(groups = {PostMapping.class})
    private Country country;
    @NotEmpty(groups = {PostMapping.class})
    private String areaCode;
    @NotEmpty(groups = {PostMapping.class})
    private String state;
}
