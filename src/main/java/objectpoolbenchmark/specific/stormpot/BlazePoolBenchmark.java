/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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
