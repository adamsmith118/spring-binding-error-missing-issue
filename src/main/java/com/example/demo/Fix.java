package com.example.demo;

import java.beans.PropertyEditor;
import java.util.List;
import java.util.Map;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

// Uncomment for my quick fix
//@Component
public class Fix {

  //@Bean
  public ErrorAttributes errorAttributes() {
    return new DefaultErrorAttributes() {
      @Override
      public Throwable getError(ServerRequest request) {
        var error =  super.getError(request);

        if (error instanceof ResponseStatusException responseStatusException && error.getCause() instanceof BindingResult bindingResult) {
          return new ResponseStatusExceptionWithBindingResult(responseStatusException, bindingResult);
        }

        return error;
      }
    };
  }

  public class ResponseStatusExceptionWithBindingResult extends ResponseStatusException implements BindingResult {
    private final BindingResult bindingResult;
    public ResponseStatusExceptionWithBindingResult(ResponseStatusException inner, BindingResult bindingResult) {
      super(inner.getStatusCode(), inner.getReason(), inner.getCause(), inner.getDetailMessageCode(), inner.getDetailMessageArguments());
      this.bindingResult = bindingResult;
    }

    @Override
    public Object getTarget() {
      return bindingResult.getTarget();
    }

    @Override
    public Map<String, Object> getModel() {
      return bindingResult.getModel();
    }

    @Override
    public Object getRawFieldValue(String field) {
      return bindingResult.getRawFieldValue(field);
    }

    @Override
    public PropertyEditor findEditor(String field, Class<?> valueType) {
      return bindingResult.findEditor(field, valueType);
    }

    @Override
    public PropertyEditorRegistry getPropertyEditorRegistry() {
      return bindingResult.getPropertyEditorRegistry();
    }

    @Override
    public String[] resolveMessageCodes(String errorCode) {
      return bindingResult.resolveMessageCodes(errorCode);
    }

    @Override
    public String[] resolveMessageCodes(String errorCode, String field) {
      return bindingResult.resolveMessageCodes(errorCode, field);
    }

    @Override
    public void addError(ObjectError error) {
      bindingResult.addError(error);
    }

    @Override
    public String getObjectName() {
      return bindingResult.getObjectName();
    }

    @Override
    public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
      bindingResult.reject(errorCode, errorArgs, defaultMessage);
    }

    @Override
    public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
      bindingResult.rejectValue(field, errorCode, errorArgs, defaultMessage);
    }

    @Override
    public List<ObjectError> getGlobalErrors() {
      return bindingResult.getGlobalErrors();
    }

    @Override
    public List<FieldError> getFieldErrors() {
      return bindingResult.getFieldErrors();
    }

    @Override
    public Object getFieldValue(String field) {
      return bindingResult.getFieldValue(field);
    }
  }
}