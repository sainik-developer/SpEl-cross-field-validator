package com.sf.customvalidator.validator;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class CrossFieldValidatorImpl implements ConstraintValidator<CrossFieldValidator, Object> {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final static String ERROR_MSG = "IF = #{ %s }, THEN = #{ %s } failed!";
    private final List<Pair<Expression, Expression>> spElExpression = new LinkedList<>();

    @Override
    public void initialize(CrossFieldValidator constraintAnnotation) {
        Arrays.stream(constraintAnnotation.conditions()).forEach(annotation -> {
            spElExpression.add(Pair.of(parser.parseExpression(annotation.IF()),
                    parser.parseExpression(annotation.THEN())));
        });
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return spElExpression.stream().allMatch(expressionPair -> handleErrorMessage(
                validate(expressionPair.getKey(), expressionPair.getValue(), value),
                expressionPair.getKey(), expressionPair.getValue(), context));
    }

    private boolean handleErrorMessage(boolean result, final Expression ifCondition,
                                       final Expression thenExpression,
                                       final ConstraintValidatorContext constraintValidatorContext) {
        if (!result) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(String
                    .format(ERROR_MSG, ifCondition.getExpressionString(),
                            thenExpression.getExpressionString())).addConstraintViolation();
        }
        return result;
    }

    private boolean validate(final Expression ifExpression, final Expression thenExpression,
                             final Object rootObj) {
        return BooleanUtils
                .toBoolean(Objects.toString(ifExpression.getValue(rootObj), Boolean.FALSE.toString())) ?
                thenExpression.getValueTypeDescriptor(rootObj).getType()
                        .isAssignableFrom(Boolean.class) ?
                        BooleanUtils.toBoolean(
                                Objects.toString(thenExpression.getValue(rootObj), Boolean.FALSE.toString())) :
                        thenExpression.getValue(rootObj) != null :
                Boolean.TRUE;
    }
}