package com.fitjourney.fitjourney.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({ProgramNotFoundException.class, UserNotFoundException.class, EnrollmentNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFoundExceptions(RuntimeException ex) {
        log.warn("Resource not found exception caught: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler({UnauthorizedProgramAccessException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleForbiddenExceptions(RuntimeException ex) {
        log.warn("Access denied exception caught: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error/403");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Security access denied exception caught: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error/403");
        modelAndView.addObject("message", "You do not have permission to access this resource.");
        return modelAndView;
    }

    @ExceptionHandler({DuplicateEnrollmentException.class, DuplicateReviewException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadRequestExceptions(RuntimeException ex) {
        log.warn("Bad request exception caught: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error/bad-request");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGlobalExceptions(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        ModelAndView modelAndView = new ModelAndView("error/500");
        modelAndView.addObject("message", "An unexpected error occurred. Please try again later.");
        return modelAndView;
    }
}
