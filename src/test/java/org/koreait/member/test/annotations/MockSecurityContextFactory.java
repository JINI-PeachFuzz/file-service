package org.koreait.member.test.annotations;

import org.koreait.member.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;

public class MockSecurityContextFactory implements WithSecurityContextFactory<MockMember> {
    @Override
    public SecurityContext createSecurityContext(MockMember annotation) {
        Member member = new Member();
        member.setSeq(annotation.seq());
        member.setEmail(annotation.email());
        member.setName(annotation.name());
        member.set_authorities(Arrays.stream(annotation.authority()).toList());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(member, null, member.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication); //  로그인 처리

        return context;
    }
}