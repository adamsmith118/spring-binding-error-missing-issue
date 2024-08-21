package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
    System.setProperty("server.error.include-message", "ALWAYS");
    System.setProperty("server.error.include-binding-errors", "ALWAYS");
		SpringApplication.run(DemoApplication.class, args);
	}

  @RestController
  public static class Controller {

    @GetMapping("/manualBindError")
    public Mono<ResponseEntity<String>> repro() {
      BindException bindException = new BindException(new RuntimeException("error"), "oh oh");
      ObjectError objectError = new ObjectError("oh oh", new String[] {"CODE"}, null, "MESSAGE");
      bindException.addError(objectError);
      throw new ResponseStatusException(400, "a reason", bindException);
    }
  }
}