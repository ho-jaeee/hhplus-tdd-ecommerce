package kr.hhplus.be.server.common.lock;

import org.aspectj.lang.ProceedingJoinPoint;

import java.util.Collections;
import java.util.List;

public interface LockKeyResolver {
    List<String> resolveKeys(ProceedingJoinPoint pjp);

    class Noop implements LockKeyResolver {
        @Override
        public List<String> resolveKeys(ProceedingJoinPoint pjp) {
            return Collections.emptyList();
        }
    }
}