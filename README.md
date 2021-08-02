# SpEl-cross-field-validator
This is an example how we can reuse the spring expression library to implement a generic cross field DTO (bean) validator in spring 


How to reduce boilerplate code by using reusable validator using spring's internal expression validator. So we don't have to write the code in our service layer nor in custom validator


Single attribute validator is available easily in spring/ hibernate/ javax validator library but what I could not find is 

we know JSR 380 brings annotations to automate POJO validation in our Java world. which Spring also uses for it's POJO validation as below 

```html
<dependency>
    <groupId>javax.validation</groupId>
    <artifactId>validation-api</artifactId>
    <version>2.0.1.Final</version>
</dependency>
```

and hibernate provides the implementationf of validation api

```html
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>6.0.13.Final</version>
</dependency>
```

spring uses it internally to provide the validation

Some example of POJO validation for Data Transmission object(DTO) is very useful as below 

```java
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
```

below is a list of some javax validator 
|                 |                   |               |                |
|---------------- | ----------------- | ------------- | -------------- |
| AssertFalse     |     Future        | NotBlank      | Pattern        |
| AssertTrue      |  FutureOrPresent  | NotEmpty      | Positive       |
| DecimalMax      | Max               | NotNull       | PositiveOrZero |
| DecimalMin      | Min               | Null          | Size           |
| Digits          | Negative          | Past          |                | 
| Email           | NegativeOrZero    | PastOrPresent |                | 

But none of the standard validator address the cross validation of field value. which is very commonly developer faces while developing, and which are resolved using custom validator at class level.
Generally developer resolves the issue as below when there is a need of cross field validation of DTO. developer will create a validator at class level for below use case 
Let's take the example of simple customer DTO as below, but there are two type of  customer as below 

public enum CustomerType {
    PERSON, ORGANIZATION
}

in case of PERSON surname is mandatory and for ORGANIZATION only name can be available as there is no surname for company name in general. to Assert that at DTO level someone has to write a validator as below

```java
import lombok.Data;

@Data
public class CustomerDTO {
    @NotEmpty(groups = PostMapping.class)
    private String name;
    @NotEmpty(groups = PostMapping.class)
    private String surname;
    @NotEmpty(groups = PostMapping.class)
    private LocalDate dob;
    private LocalDate doi;
    private List<@Valid AddressDTO> addresses;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    @NotEmpty(groups = PostMapping.class)
    private CustomerType customerType;
}
```
```java
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { CustomerValidatorImpl.class })
@Documented
public @interface CustomerValidator {
    String message() default "failed!";
    Class<?>[] groups() default { };
    Class<? extends Payload>[]payload() default {};
    SpElCrossFieldCondition[] conditions();
}
``` 
```java
public class CustomerValidatorImpl implements ConstraintValidator<CrossFieldValidator, CustomerDTO> {
 @Override
 public boolean isValid(CustomerDTO customerDTO, ConstraintValidatorContext context) {
     return (custmerDTO.getCustomerType() == ORGANIZATION  
                && StringUtils.isEmpty(custmerDTO.getSurname())) 
            || (custmerDTO.getCustomerType() == PERSON 
                && !StringUtils.isEmpty(custmerDTO.getSurname())) ;
 }
}
``` 
Which is easy to do but not very reusable way to implement it, suppose there is next system requirement for CustomerDTO is **dob** is applicable for customer *PERSON* and **doi** is applicable for *ORGANIZATION* . which will require modification of CustomerValidatorImpl as below 




```java
public class CustomerValidatorImpl implements ConstraintValidator<CrossFieldValidator, CustomerDTO> {
 @Override
 public boolean isValid(CustomerDTO customerDTO, ConstraintValidatorContext context) {
     return (custmerDTO.getCustomerType() == ORGANIZATION  
                && StringUtils.isEmpty(custmerDTO.getSurname()) 
                && Objects.isNull(custmerDTO.getDOB()) 
                && Objects.nonNull(custmerDTO.getDOI())) 
                || (custmerDTO.getCustomerType() == PERSON 
                && !StringUtils.isEmpty(custmerDTO.getSurname()) 
                && Objects.isNull(custmerDTO.getDOI()) 
                && Objects.nonNull(custmerDTO.getDOB()));
 }
}
``` 
but what is there is conditional validation required for `AddressDTO` nested object. In that case is current approach we have to write an another validator related to AddressDTO. 

which  gave me thought that if we can easily sort it out with a generic solution. 

Here is my take on this generic validation issue with below approach

if we can declare at DTO level it gives below positives 

1. generic code so no need to write logic useung varbose language and fremework systax 
2. higher code readbilty as validation is written upfront on DTO 


```java
@Data
@CrossFieldValidator(groups = {PostMapping.class, PutMapping.class}, conditions = {
 @SpElCrossFieldCondition(IF = "customerType == T(com.sf.customvalidator.constant.CustomerType).ORGANIZATION", 
                            THEN = "surname==null"),
 @SpElCrossFieldCondition(IF = "customerType == T(com.sf.customvalidator.constant.CustomerType).PERSON", 
                            THEN = "surname!=null AND !surname.isEmpty()")
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
```

Here we take advantage of spring expression langauge to evaluate the validation condition, 
you can have a look at implemenation I am here to  talk about how it should be used rather internal details as those are not very interesting. 
[CrossFieldValidatorImpl.java](https://github.com/sainik-developer/SpEl-cross-field-validator/blob/main/src/main/java/com/sf/customvalidator/validator/CrossFieldValidatorImpl.java)
 
 