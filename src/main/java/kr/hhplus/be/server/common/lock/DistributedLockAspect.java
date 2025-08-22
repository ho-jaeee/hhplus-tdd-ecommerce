package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Aspect
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redisson;
    private final ApplicationContext appCtx;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(ann)")
    public Object around(ProceedingJoinPoint pjp, DistributedLock ann) throws Throwable {
        // 1) SpEL 키
        List<String> keys = new ArrayList<>(resolveKeysFromSpel(pjp, ann.keys()));

        // 2) 리졸버 키
        Class<? extends LockKeyResolver> resolverType = ann.resolver();
        if (resolverType != null && resolverType != LockKeyResolver.class) {
            LockKeyResolver resolver = getResolver(resolverType);
            List<String> rkeys = resolver.resolveKeys(pjp);
            if (rkeys != null) keys.addAll(rkeys);
        }

        // 3) 정리(트림/중복제거/필터)
        keys = keys.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (keys.isEmpty()) return pjp.proceed();

        if (ann.sortKeys() && keys.size() > 1) {
            keys = new ArrayList<>(new TreeSet<>(keys)); // 사전순 정렬
        }

        // 4) 락 인스턴스 구성(단일 or 멀티)
        final RLock lock = (keys.size() == 1)
                ? redisson.getLock(keys.get(0))
                : new RedissonMultiLock(keys.stream().map(redisson::getLock).toArray(RLock[]::new));

        boolean acquired = false;
        try {
            if (ann.leaseMs() > 0) {
                acquired = lock.tryLock(ann.waitMs(), ann.leaseMs(), TimeUnit.MILLISECONDS);
            } else {
                // leaseMs <= 0: 워치독 모드(자동 연장)
                acquired = lock.tryLock(ann.waitMs(), TimeUnit.MILLISECONDS);
            }
            if (!acquired) {
                if (ann.throwOnTimeout()) {
                    throw new ConcurrencyLockException("LOCK_TIMEOUT: " + keys);
                } else {
                    return null;
                }
            }
            return pjp.proceed();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 대기 중 인터럽트", ie);
        } finally {
            try {
                if (acquired && lock.isHeldByCurrentThread()) lock.unlock();
            } catch (Exception ignore) {}
        }
    }

    private List<String> resolveKeysFromSpel(ProceedingJoinPoint pjp, String[] expressions) {
        if (expressions == null || expressions.length == 0) return List.of();

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        String[] paramNames = sig.getParameterNames();
        Object[] args = pjp.getArgs();

        EvaluationContext ctx = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                ctx.setVariable(paramNames[i], args[i]); // #command 등 이름 접근
                ctx.setVariable("p" + i, args[i]);       // #p0 형식 접근
            }
        }

        List<String> out = new ArrayList<>();
        for (String exp : expressions) {
            if (exp == null || exp.isBlank()) continue;
            Object v = parser.parseExpression(exp).getValue(ctx);
            if (v == null) continue;
            if (v instanceof Collection<?> c) {
                for (Object o : c) if (o != null) out.add(o.toString());
            } else {
                out.add(v.toString());
            }
        }
        return out;
    }

    private LockKeyResolver getResolver(Class<? extends LockKeyResolver> type) {
        try {
            return appCtx.getBean(type);
        } catch (NoSuchBeanDefinitionException e) {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException("LockKeyResolver 인스턴스화 실패: " + type, ex);
            }
        }
    }
}