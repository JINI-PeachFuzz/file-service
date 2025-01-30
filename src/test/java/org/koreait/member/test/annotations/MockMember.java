package org.koreait.member.test.annotations;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = org.koreait.member.test.annotations.MockSecurityContextFactory.class)
public @interface MockMember {
    long seq() default 1L;
    String email() default "user01@test.org";
    String name() default "사용자01";
    org.koreait.member.contants.Authority[] authority() default { org.koreait.member.contants.Authority.USER };
}