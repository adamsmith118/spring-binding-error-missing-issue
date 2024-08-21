# Spring Boot Issue: Missing Bind Errors

Since [Boot 2.3](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.3-Release-Notes) binding errors are no longer included in the default error page by default.

The resolution, until [this commit](https://github.com/spring-projects/spring-boot/commit/82b218937c50da213294413f1f5e8186d854f073), was to set this property accordingly.

```server.error.include-binding-errors=ALWAYS```

Unfortunately this no longer works in Boot 3.2.6+.

## Expected Behaviour/Repro

Full repro [here](https://github.com/adamsmith118/spring-binding-error-missing-issue)

### Code

```java
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
```

When we hit http://localhost:8080/manualBindError

### Expected

Something resembling the below - with errors intact.

```json
{
  "timestamp": "2024-08-21T13:20:39.377+00:00",
  "path": "/manualBindError",
  "status": 400,
  "error": "Bad Request",
  "message": "a reason",
  "requestId": "d4b54677-3",
  "errors": [
    {
      "codes": [
        "CODE"
      ],
      "arguments": null,
      "defaultMessage": "MESSAGE",
      "objectName": "oh oh",
      "code": "CODE"
    }
  ]
}
```

### Actual

Errors are missing.

```json
{
  "timestamp": "2024-08-21T13:27:27.504+00:00",
  "path": "/manualBindError",
  "status": 400,
  "error": "Bad Request",
  "requestId": "ae84da6d-1",
  "message": "a reason"
}
```

## Root Cause

The code below was removed from [`DefaultErrorAttributes`](https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/web/reactive/error/DefaultErrorAttributes.java).

```java
	private Throwable determineException(Throwable error) {
		if (error instanceof ResponseStatusException) {
			return (error.getCause() != null) ? error.getCause() : error;
		}
		return error;
	}
```

This is problematic if the cause is an instance of `BindingResult` as this is no longer true, so the errors aren't added regardless of property value....

```java
	private void handleException(Map<String, Object> errorAttributes, Throwable error,
			MergedAnnotation<ResponseStatus> responseStatusAnnotation, boolean includeStackTrace) {
		Throwable exception;
		if (error instanceof BindingResult bindingResult) {
			errorAttributes.put("message", error.getMessage());
			errorAttributes.put("errors", bindingResult.getAllErrors());
			exception = error;
		}
```

## Questions

I can fix this by exposing a custom `ErrorAttributes` Bean in each app.  However, is this the intention or is this a bug?  If it's the former then a documentation update would be handy...