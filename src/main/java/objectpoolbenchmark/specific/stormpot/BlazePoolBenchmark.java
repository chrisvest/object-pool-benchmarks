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
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.logic.BlackHole;
import stormpot.Config;
import stormpot.Expiration;
import stormpot.SlotInfo;
import stormpot.Timeout;
import stormpot.bpool.BlazePool;

@State(Scope.Benchmark)
public class BlazePoolBenchmark {
  private static final Timeout timeout = new Timeout(1, TimeUnit.MINUTES);
  private static final Object tokenObject = new Object();

  private BlazePool<GenericPoolable> pool;
  private AtomicInteger atomicInteger;
  private ThreadLocal<Object> threadLocal;


  @GenerateMicroBenchmark
  public void AtomicInteger_compareAndSet(BlackHole blackHole) {
    blackHole.consume(atomicInteger.compareAndSet(0, 1) && atomicInteger.compareAndSet(1, 0));
  }

  @GenerateMicroBenchmark
  public void AtomicInteger_compareAndSetAndLazySet(BlackHole blackHole) {
    blackHole.consume(atomicInteger.compareAndSet(0, 1));
    atomicInteger.lazySet(0);
  }

  @GenerateMicroBenchmark
  public void AtomicInteger_compareAndSetAndSet(BlackHole blackHole) {
    blackHole.consume(atomicInteger.compareAndSet(0, 1));
    atomicInteger.set(0);
  }

  @GenerateMicroBenchmark
  public void AtomicInteger_compareAndSetAndWeakCompareAndSet(BlackHole blackHole) {
    blackHole.consume(atomicInteger.compareAndSet(0, 1) && atomicInteger.weakCompareAndSet(1, 0));
  }

  @GenerateMicroBenchmark
  public void AtomicInteger_weakCompareAndSetAndWeakCompareAndSet(BlackHole blackHole) {
    blackHole.consume(atomicInteger.weakCompareAndSet(0, 1) && atomicInteger.weakCompareAndSet(1, 0));
  }

  @GenerateMicroBenchmark
  public void AtomicInteger_weakCompareAndSetAndLazySet(BlackHole blackHole) {
    blackHole.consume(atomicInteger.weakCompareAndSet(0, 1));
    atomicInteger.lazySet(0);
  }

  @GenerateMicroBenchmark
  public Object ThreadLocal_get() {
    return threadLocal.get();
  }

  @GenerateMicroBenchmark
  public void BlazePool_claimRelease() throws InterruptedException {
    pool.claim(timeout).release();
  }

  @GenerateMicroBenchmark
  public void BlazePool_claimRelease_withBH(BlackHole blackHole) throws InterruptedException {
    GenericPoolable obj = pool.claim(timeout);
    blackHole.consume(obj);
    obj.release();
  }


  @Setup
  public void createPool() throws InterruptedException {
    Config<GenericPoolable> config = new Config<>().setAllocator(new GenericAllocator());
    config.setExpiration(new Expiration<GenericPoolable>() {
      @Override
      public boolean hasExpired(SlotInfo<? extends GenericPoolable> slotInfo) {
        return false;
      }
    });
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
