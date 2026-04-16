package com.soundboard.soundboard.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@AuthenticationPrincipal
@AuthenticationPrincipal(expression = "claims['{claim}']") // - this is the expression that
// will be used to extract the username from the jwt token and inject it into the method
// parameter annotated with @CurrentUser. this allows us to easily access the username of the
// currently authenticated user in our controller methods without having to
// manually extract it from the security context or jwt token.
public @interface CurrentUser {
  String claim() default "username";
}
