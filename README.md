# General purpose cross field POJO/DTO validator
## What is it ? 
It's a leap to more general purpose java validator. It uses spring expression library to implement a generic cross field POJO (bean) validator.It reduces boilerplate code by using reusable 
custom validator using spring's internal expression evaluator and help us avoid writing DTO specific custom validator or service layer logic. It makes code more readable.
 
Known benefits:  
1. General purpose code to avoid writing POJO/DTO specific custom validator or service layer verbose if-else logic
2. Higher code readability as validation constraints are written in POJO/DTO. 

## what is cross field validation ? 
Below is a sample class which as few field. But suppose there is a requirements as below 
Validation requirements 
1. `name` should be not empty string. 
2. `surname` should be non-empty for __PERSON__ and empty for __ORGANIZATION__ customerType.
3. `dob` should be non-empty for __PERSON__ customerType but for __ORGANIZATION__ should be empty.
4. `doi` should be empty for __PERSON__ but non-empty for __ORGANIZATION__ customerType.
5. `addresses` should be size of one for __ORGANIZATION__ and can be two but at least one for __PERSON__ customerType.
6. `customerType` should not be empty and one of the value of enum 
[CustomerType.java](https://github.com/sainik-developer/SpEl-cross-field-validator/blob/main/src/main/java/com/sf/customvalidator/constant/CustomerType.java)


#### What is sorted here to address above issue ? 
What if we just can declare our cross-field conditional restriction using annotation at POJO/DTO class as below
```java
public enum CustomerType {
    PERSON, ORGANIZATION
}
```

```java
@Data
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
    @NotEmpty(groups = PostMapping.class)
    private CustomerType customerType;
}
```

```java
@Data
@CrossFieldValidator(groups = {PostMapping.class}, conditions = {
        @SpElCrossFieldCondition(IF = "country == T(com.sf.customvalidator.constant.Country).US OR country == T(com.sf.customvalidator.constant.Country).DE", THEN = "areaCode != null && areaCode.length == 5"),
        @SpElCrossFieldCondition(IF = "country == T(com.sf.customvalidator.constant.Country).IND", THEN = "areaCode != null && areaCode.length == 6")
})
public class AddressDTO {
    @NotEmpty(groups = {PostMapping.class})
    private String houseNo;
    @NotEmpty(groups = {PostMapping.class})
    private String lane;
    @NotEmpty(groups = {PostMapping.class})
    private Country country;
    @NotEmpty(groups = {PostMapping.class})
    private String areaCode;
    @NotEmpty(groups = {PostMapping.class})
    private String state;
}
```
What we just achieve by above annotation approach 

1. generic code so no need to write logic using verbose language and framework syntax 
2. higher code readability as validation is written upfront on DTO 

#### How to write condition for the validation ? 
We have to use our familiar spring expression language (SpEl) to expression our `IF` and `THEN` condition, Documentation of spring expression langauge can be found in 
[SpEl](https://docs.spring.io/spring-framework/docs/3.0.x/reference/expressions.html) 

#### Implementation of generic validater is here

[SpEl Cross field validator](https://github.com/sainik-developer/SpEl-cross-field-validator)
you can have a look at implemenation I am here to  talk about how it should be used rather internal details as those are not very interesting. 
[CrossFieldValidatorImpl.java](https://github.com/sainik-developer/SpEl-cross-field-validator/blob/main/src/main/java/com/sf/customvalidator/validator/CrossFieldValidatorImpl.java)
 
 