package kr.hhplus.be.server.point.component;

@FunctionalInterface
public interface CommandLineRunner {
    void run(String... args) throws Exception;
}