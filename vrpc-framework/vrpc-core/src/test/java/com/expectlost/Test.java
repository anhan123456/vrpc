package com.expectlost;

import lombok.Data;

import java.io.*;
import static java.util.Optional.ofNullable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import static java.lang.System.out;

public class Test extends A {
    @Override
    public String getSay() {
        return "子类定义说话内容";
    }

    public static void main(String[] args) throws FileNotFoundException {
        new PrintStream(new File("D://a.txt")).println("123");
    }
}

/**
 * 子类可以定义说话的内容 但是不能更改说话的方式
 */
@Data
abstract class A {
    public final void say() {
        System.out.println(getSay());
    }
    public abstract String getSay();
}
