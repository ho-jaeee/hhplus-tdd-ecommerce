package kr.hhplus.be.server.common.lock;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    Class<? extends LockKeyResolver> resolver() default LockKeyResolver.Noop.class;
    String[] keys() default {};
    long waitMs() default 5000;
    long leaseMs() default 0;
    boolean throwOnTimeout() default true;
    boolean sortKeys() default true;
}
