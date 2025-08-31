//package com.mykyda.websocketdemo.security.exception;
//
//import org.springframework.data.crossstore.ChangeSetPersister;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
//
//    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
//    public ResponseEntity<String> handleNotFoundException(ChangeSetPersister.NotFoundException e) {
//        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    public Map<String, Object> handleAllExceptions(Exception ex) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("error", "Internal Server Error");
//        response.put("message", ex.getMessage());
//        return response;
//    }
//}