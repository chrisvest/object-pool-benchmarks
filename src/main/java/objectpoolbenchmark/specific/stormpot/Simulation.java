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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import objectpoolbenchmark.suite.stormpot.GenericAllocator;
import objectpoolbenchmark.suite.stormpot.GenericPoolable;
import objectpoolbenchmark.suite.stormpot.GenericPoolableExpiration;
import org.openjdk.jmh.annotations.*;
import stormpot.Pool;
import stormpot.Timeout;


@Threads(4)
@Warmup(iterations = 20)
@Measurement(iterations = 10)
@Fork(5)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class Simulation {
  private static final Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
  private ThreadLocal<AtomicLong> tlr;
  private Pool<GenericPoolable> pool;
  private Callable<Object> objectCreator = new Callable<Object>() {
    @Override
    public Object call() throws Exception {
      return new Object();
    }
  };

  @Setup
  public void setUp() {
    tlr = new ThreadLocal<>();
    pool = Pool.from(new GenericAllocator()).setSize(10).setExpiration(new GenericPoolableExpiration()).build();
  }

  @TearDown
  public void tearDown() throws InterruptedException {
    tlr.remove();
    tlr = null;
    pool.shutdown().await(timeout);
  }

  @Benchmark
  public Object getSetSet(Simulation sim) {
    AtomicLong ai = sim.tlr.get();
    if (ai == null) {
      ai = new AtomicLong(42);
      sim.tlr.set(ai);
    }
    if (!ai.compareAndSet(42, 1337)) {
      throw new AssertionError("compareAndSet failed");
    }
    ai.lazySet(42);
    return ai;
  }

  @Benchmark
  public Object newObject(Simulation sim) throws Exception {
    return sim.objectCreator.call();
  }

  @Benchmark
  public Object claimReleaseWithReturn(Simulation sim) throws InterruptedException {
    GenericPoolable obj = sim.pool.claim(timeout);
    obj.release();
    return obj;
  }

  @Benchmark
  public void claimReleaseWithoutReturn(Simulation sim) throws InterruptedException {
    GenericPoolable obj = sim.pool.claim(timeout);
    obj.release();
  }

  @Benchmark
  public Object returnSim(Simulation sim) {
    return sim;
  }
}
