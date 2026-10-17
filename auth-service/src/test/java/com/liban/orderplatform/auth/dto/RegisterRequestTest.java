package com.liban.orderplatform.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeEach
    void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void shouldAcceptValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest(
                "libanr4243@gmail.com",
                "liban@123456"
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void shouldRejectInvalidEmail() {
        RegisterRequest request = new RegisterRequest(
                "li-ban-r",
                "liban@12345"
        );

        assertThat(validator.validate(request))
                .extracting(violation ->
                        violation.getPropertyPath().toString())
                .contains("email");
    }

    @Test
    void shouldRejectInvalidPassword() {
        RegisterRequest request = new RegisterRequest(
                "libanr4243@gmail.com",
                "li"
        );

        assertThat(validator.validate(request))
                .extracting(violation ->
                        violation.getPropertyPath().toString())
                .contains("password");
    }

    @Test
    void shouldRejectInvalidEmailAndPassword() {
        RegisterRequest request = new RegisterRequest(
                "",
                ""
        );

        assertThat(validator.validate(request))
                .extracting(fields ->
                        fields.getPropertyPath().toString())
                .contains("email", "password");
    }
}