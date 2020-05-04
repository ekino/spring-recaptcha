# Spring reCaptcha

Library providing a custom filter to validate reCaptcha

## Requirements

* JDK 8 +
* Spring Boot 2.2+ MVC application

## Supported reCaptcha version

* [X] reCaptcha v2
* [ ] reCaptcha v3

## Usage 

To add the custom filter, you need to include the dependency and configure required property (see [Required configuration](#required-configuration)).

```kotlin
dependencies {
  ...
  implementation("com.ekino.oss.spring:spring-recaptcha:1.0.0")
  ...
}
```

When calling an url requiring reCaptcha validation, you must send the reCaptcha response parameter, 
either in a request parameter or in a request header.

By default the parameter (or header) name is : `g-recacptcha-response` (see [Change default reponse parameter name](#change-default-response-parameter-name))


## Configuration

### Required configuration

The library comes with a configuration by default.
However, you must configure the secret before using it.

```yaml
security:
  recaptcha:
    secret: <your_secret>
```

By default, no url require reCaptcha validation. 
You must configure the `url-patterns` property to enable reCaptcha validation for your endpoints.
 
The patterns are a list of regular expressions matching the requested url.

Example :
```yaml
security:
  recaptcha:
    url-patterns:
      - "/api/resources/[^/]*/sub-resource"
      - "/api/recaptcha/.*"
```

### Other configuration

#### Disable filter

ReCaptcha validation filter can be disabled by setting the property :

```yaml
security:
  recaptcha:
    enabled: false
``` 

#### Filter methods

By default, every urls matching one of the pattern for the `POST` method will need reCaptcha validation. 
You can modify the filtered methods by setting the properties :

```yaml
security:
  recaptcha:
    filtered-methods:
      - POST
      - PUT
```  

**Note:** The filtered method apply for all the url patterns. 

#### By-passing the filter

Connection or validation issues could occur with external services. 

To prevent re-deployment of the whole application to disable the filter and provide a quick fix, 
you can configure a private key and send it in a custom header `X-ReCaptcha-ByPass-Key` to by-pass the reCaptcha validation.

```yaml
security:
  recaptcha:
    by-pass-key: <your_by_pass_private_key>  
```

#### Change default response parameter name

To modify the default request parameter (or header) name, you can set the property :

```yaml
security:
  recaptcha:
    response-name: other-response-parameter-name
```

## Handle failure

The library provide you a default failure handling. 

If the response request parameter is missing, the answer will be a `400 BAD REQUEST` with message :
```json
{
    "code": "recaptcha.missing.response",
    "message": "Unable to retrieve recaptcha response parameter. Please check configuration if endpoint really need reCaptcha validation or if response parameter name is correct."
}
```  

If the validation fails, the response will be a `403 FORBIDDEN` with message :
```json
{
    "code": "recaptcha.validation.failed",
    "details": [ <list of error codes> ],
    "message": "Validation failed for reCaptcha response."
}
```

### Override default failure handling

You override the default failure handling by creating your own custom failure handling service :
```kotlin
@Service
class CustomReCaptchaFailureService(objectMapper: ObjectMapper): ReCaptchaFailureService(objectMapper) {

    override fun handleMissingResponseParameter(response: HttpServletResponse) {
      super.handleMissingResponseParameter(response)
    }

    override fun handleValidationFailure(validationFailure: ReCaptcaValidationResult.Failure, response: HttpServletResponse) {
      super.handleValidationFailure(validationFailure, response)
    }
}
```

To handle missing response request parameter, you must override the `handleMissingResponseParameter` method.
To handle validatino failure, you must override the `handleValidationFailure` method.

A utility function `fun handleResponse(response: HttpServletResponse, status: HttpStatus, responseContent: Any)` is present 
to help you create you response with custom `HttpStatus` and content.
