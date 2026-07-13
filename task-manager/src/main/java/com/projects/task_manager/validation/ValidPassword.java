package com.projects.task_manager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    // The default error message if validation fails
    String message() default "Password must be at least 8 characters long to meet security standards";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}