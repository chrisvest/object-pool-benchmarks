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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import objectpoolbenchmark.suite.stormpot.GenericAllocator;
import objectpoolbenchmark.suite.stormpot.GenericPoolable;
import objectpoolbenchmark.suite.stormpot.GenericPoolableExpiration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import stormpot.Config;
import stormpot.Timeout;
import stormpot.bpool.BlazePool;

@State(Scope.Benchmark)
public class BlazePoolBenchmark {
  private static final Timeout timeout = new Timeout(1, TimeUnit.MINUTES);
  private static final Object tokenObject = new Object();

  private BlazePool<GenericPoolable> pool;
  private AtomicInteger atomicInteger;
  private ThreadLocal<Object> threadLocal;


  @Benchmark
  public void AtomicInteger_compareAndSet(Blackhole blackhole) {
    blackhole.consume(atomicInteger.compareAndSet(0, 1) && atomicInteger.compareAndSet(1, 0));
  }

  @Benchmark
  public void AtomicInteger_compareAndSetAndLazySet(Blackhole blackhole) {
    blackhole.consume(atomicInteger.compareAndSet(0, 1));
    atomicInteger.lazySet(0);
  }

  @Benchmark
  public void AtomicInteger_compareAndSetAndSet(Blackhole blackhole) {
    blackhole.consume(atomicInteger.compareAndSet(0, 1));
    atomicInteger.set(0);
  }

  @Benchmark
  public void AtomicInteger_compareAndSetAndWeakCompareAndSet(Blackhole blackhole) {
    blackhole.consume(atomicInteger.compareAndSet(0, 1) && atomicInteger.weakCompareAndSet(1, 0));
  }

  @Benchmark
  public void AtomicInteger_weakCompareAndSetAndWeakCompareAndSet(Blackhole blackhole) {
    blackhole.consume(atomicInteger.weakCompareAndSet(0, 1) && atomicInteger.weakCompareAndSet(1, 0));
  }

  @Benchmark
  public void AtomicInteger_weakCompareAndSetAndLazySet(Blackhole blackhole) {
    blackhole.consume(atomicInteger.weakCompareAndSet(0, 1));
    atomicInteger.lazySet(0);
  }

  @Benchmark
  public Object ThreadLocal_get() {
    return threadLocal.get();
  }

  @Benchmark
  public void BlazePool_claimRelease() throws InterruptedException {
    pool.claim(timeout).release();
  }

  @Benchmark
  public void BlazePool_claimRelease_withBH(Blackhole blackhole) throws InterruptedException {
    GenericPoolable obj = pool.claim(timeout);
    blackhole.consume(obj);
    obj.release();
  }


  @Setup
  public void createPool() throws InterruptedException {
    Config<GenericPoolable> config = new Config<>().setAllocator(new GenericAllocator());
    config.setExpiration(new GenericPoolableExpiration());
    pool = new BlazePool<>(config);
    pool.claim(timeout).release();
    atomicInteger = new AtomicInteger();
    threadLocal = new ThreadLocal<>();
    threadLocal.set(tokenObject);
  }

  @TearDown
  public void shutPoolDown() throws InterruptedException {
    pool.shutdown().await(timeout);
  }
}
