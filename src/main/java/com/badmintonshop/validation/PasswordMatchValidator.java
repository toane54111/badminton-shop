package com.badmintonshop.validation;

import com.badmintonshop.dto.auth.RegisterRequest;
import com.badmintonshop.dto.auth.ResetPasswordRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for @PasswordMatch annotation
 * Works with both RegisterRequest and ResetPasswordRequest
 */
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        String password = null;
        String confirmPassword = null;

        if (obj instanceof RegisterRequest request) {
            password = request.getPassword();
            confirmPassword = request.getConfirmPassword();
        } else if (obj instanceof ResetPasswordRequest request) {
            password = request.getPassword();
            confirmPassword = request.getConfirmPassword();
        }

        if (password == null || confirmPassword == null) {
            return true; // Let @NotBlank handle null cases
        }

        boolean isValid = password.equals(confirmPassword);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
