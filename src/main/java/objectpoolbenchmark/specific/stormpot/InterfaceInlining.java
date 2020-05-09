/*
 * Copyright (C) 2014 Chris Vest (mr.chrisvest@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package objectpoolbenchmark.specific.stormpot;


import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

@Threads(4)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(0)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class InterfaceInlining {
  interface A {
    int compute();
  }

  static class A1 implements A {
    @Override
    public int compute() {
      return ThreadLocalRandom.current().nextInt();
    }
  }

  interface B {
    int compute();
  }

  static class B1 implements B {
    @Override
    public int compute() {
      return ThreadLocalRandom.current().nextInt();
    }
  }

  static class B2 implements B {
    @Override
    public int compute() {
      return ThreadLocalRandom.current().nextInt();
    }
  }

  interface C {
    int compute();
  }

  static class C1 implements C {
    @Override
    public int compute() {
      return ThreadLocalRandom.current().nextInt();
    }
  }

  static class C2 implements C {
    @Override
    public int compute() {
      return ThreadLocalRandom.current().nextInt();
    }
  }

  static class C3 implements C {
    @Override
    public int compute() {
      return ThreadLocalRandom.current().nextInt();
    }
  }

  static class D {
    public int compute() {
      return ThreadLocalRandom.current().nextInt();
    }
  }

  private A a;
  private B b;
  private C c;
  private D d;

  @Setup
  public void setUp() {
    a = new A1();
    a.compute();
    b = new B1();
    b.compute();
    b = new B2();
    b.compute();
    c = new C1();
    c.compute();
    c = new C2();
    c.compute();
    c = new C3();
    c.compute();
    d = new D();
    d.compute();
  }

  @Benchmark
  public int baseline() {
    return ThreadLocalRandom.current().nextInt();
  }

  @Benchmark
  public int computeA() {
    return a.compute();
  }

  @Benchmark
  public int computeB() {
    return b.compute();
  }

  @Benchmark
  public int computeC() {
    return c.compute();
  }

  @Benchmark
  public int computeD() {
    return d.compute();
  }
}
/*
Benchmark                            Mode  Samples     Score    Error   Units
o.s.s.InterfaceInlining.baseline    thrpt       50  1040.125 ± 10.277  ops/us
o.s.s.InterfaceInlining.computeA    thrpt       50   885.408 ± 11.549  ops/us
o.s.s.InterfaceInlining.computeB    thrpt       50   880.334 ±  9.838  ops/us
o.s.s.InterfaceInlining.computeC    thrpt       50   864.799 ± 15.878  ops/us
o.s.s.InterfaceInlining.computeD    thrpt       50  1005.013 ±  7.345  ops/us
 */
