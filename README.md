Generic inter-field bean validator using SpEl
---
## What is it ? 
It's a generic validator using spring expression language(SpEl) to specify validation constraints.It improves readability by keeping validation logic in 
same POJO/DTO and it's a Type(Class) level validator where inter field constraints can be specified using well known SpEl. 

Known benefits:
1. Avoid witting custom validator for different beans.
2. High readability by keeping constraints in same bean class.
3. Allow to express validation logic by SpEl (Spring expression language)

## Technical background 
### Brief history of validator in Java
    
Spring(J2EE)) is well familiar with POJO/DTO validator for a while, it's well organized, standardised by java community 
and used by most. It's captured in JSR(Java Specification Requests)-380 and hibernate community implemented and maintained some of the most well known validators. 
Spring includes it as part of it's dependency hence has all the feature of validation as per JSR-380.

#### What's addressed by JSR-380
`javax.validator.validation-api` is code for the JSR-380 requirements. It gives a framework to extend the validator as below.

```java
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { CustomValidatorImpl.class })
public @interface CustomValidator {
    String message() default "failed!";

    Class<?>[] groups() default { };

    Class<? extends Payload>[]payload() default {};
}
```

```java
public class CustomValidatorImpl implements ConstraintValidator<CrossFieldValidator, Object> {

    @Override
    public void initialize(CrossFieldValidator constraintAnnotation) {
        // TODO
    }
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // TODO
    }
}
```
Default validators are implemented by hibernate community. Framework allows user to implement their requirements when default validators don't support the validation constraints.
Default validators are below 
|                 |                   |               |                |
|---------------- | ----------------- | ------------- | -------------- |
| AssertFalse     | Future            | NotBlank      | Pattern        |
| AssertTrue      | FutureOrPresent   | NotEmpty      | Positive       |
| DecimalMax      | Max               | NotNull       | PositiveOrZero |
| DecimalMin      | Min               | Null          | Size           |
| Digits          | Negative          | Past          |                | 
| Email           | NegativeOrZero    | PastOrPresent |                | 

It's implemented by hibernate community and being used by spring

```html
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
</dependency>
```

### What is new
#### What is inter-field bean validator ? 
Below is a sample class with fields and validation requirements as below.


1. `name` must be not empty string. 
2. `surname` must be non-empty for __PERSON__ and must be empty for __ORGANIZATION__ customerType.
3. `dob` must be non-empty for __PERSON__ customerType but for __ORGANIZATION__ must be empty.
4. `doi` must be empty for __PERSON__ but must be non-empty for __ORGANIZATION__ customerType.
5. `addresses` must be size of one for __ORGANIZATION__ and can be two but at least one for __PERSON__ customerType.
6. `customerType` must not be empty and one of the value of enum 
[CustomerType.java](https://github.com/sainik-developer/SpEl-cross-field-validator/blob/main/src/main/java/com/sf/customvalidator/constant/CustomerType.java)

##### Below is a try to impose validations using `javax.validator`. 
```java
public enum CustomerType {
    PERSON, ORGANIZATION
}
```
```java
public class CustomerDTO {
    @NotEmpty(groups = PostMapping.class)
    private String name;
    @NotEmpty(groups = PostMapping.class)
    private String surname;
    @NotEmpty(groups = PostMapping.class)
    private LocalDate dob;
    @NotEmpty(groups = PostMapping.class)
    private LocalDate doi;
    @Size(min=1, max=2)
    private List<@Valid AddressDTO> addresses;
    @NotEmpty(groups = PostMapping.class)
    private CustomerType customerType;
}
```
It's not possible to enforce the restriction using default validators as there is no validator which cares about inter field constraints.

Hence developer will opt for custom validator for `CustomerDTO` or write logic in service layer as below.
##### Below is the way explained how it can done by custom validator.
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
                && StringUtils.isEmpty(custmerDTO.getSurname()) 
                && Objects.isNull(custmerDTO.getDOB()) 
                && Objects.nonNull(custmerDTO.getDOI()) 
                && Objects.nonNull(addresses) && addresses.size() == 1)
                || (custmerDTO.getCustomerType() == PERSON 
                && !StringUtils.isEmpty(custmerDTO.getSurname()) 
                && Objects.isNull(custmerDTO.getDOI()) 
                && Objects.nonNull(custmerDTO.getDOB())
                && Objects.nonNull(addresses) && addresses.size() >= 1 && addresses.size() <= 2);
 }
}
``` 

But what is there is conditional cross-field validation required for `AddressDTO` nested object. In that case is current approach we have to write an another custom validator 
related to AddressDTO. 

#### What is sorted here to address above issue ? 
What if we just can declare our cross-field conditional restriction using annotation at POJO/DTO class as below

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

#### How to write condition for the validation ? 
We have to use our familiar spring expression language (SpEl) to expression our `IF` and `THEN` condition, Documentation of spring expression langauge can be found in 
[SpEl](https://docs.spring.io/spring-framework/docs/3.0.x/reference/expressions.html) 

#### Implementation of generic validater is here

[SpEl Cross field validator](https://github.com/sainik-developer/SpEl-cross-field-validator)
you can have a look at implemenation I am here to  talk about how it should be used rather internal details as those are not very interesting. 
[CrossFieldValidatorImpl.java](https://github.com/sainik-developer/SpEl-cross-field-validator/blob/main/src/main/java/com/sf/customvalidator/validator/CrossFieldValidatorImpl.java)
    
 