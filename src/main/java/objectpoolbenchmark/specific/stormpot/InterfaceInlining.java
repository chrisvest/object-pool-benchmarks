/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
