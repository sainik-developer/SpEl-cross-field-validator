# SpEl-cross-field-validator
This is an example how we can reuse the spring expression library to implement a generic cross field DTO (bean) validator in spring 


How to reduce boilerplat code by using reuseable validator using spring's internal expression validator. So we don't have to write the code in our service layer nor in custom validator


Single attribute validator is avaialble easily in spring/ hibernet/ javax validator library but what I could not find is 

we know JSR 380 brings annotations to automate POJO valuidation in our Java wrold. which Spring also uses for it's POJO validation as below 

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

Some exaple of POJO validation for Data Transmission object(DTO) is very useful as below 


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

|  Validator      |
| --------------- |
| AssertFalse     |
| AssertTrue      |
| DecimalMax      |
| DecimalMin      |
| Digits          |
| Email           |
| Future          |  
| FutureOrPresent |
| Max             |  
| Min             |
| Negative        |
| NegativeOrZero  |
| NotBlank        |
| NotEmpty        | 
| NotNull         |  
| Null            |
| Past            | 
| PastOrPresent   |
| Pattern         | 
| Positive        |  
| PositiveOrZero  | 
| Size            |

But non of the standard validator address the cross validation of field value. which is very commonly developer faces while developing, and which are resolved using custom validator at class level.
 