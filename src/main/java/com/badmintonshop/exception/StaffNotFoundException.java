package com.badmintonshop.exception;

/**
 * Exception thrown when a staff member is not found
 */
public class StaffNotFoundException extends RuntimeException {

    public StaffNotFoundException(String email) {
        super("Staff not found with email: " + email);
    }

    public StaffNotFoundException(Long id) {
        super("Staff not found with id: " + id);
    }
}
