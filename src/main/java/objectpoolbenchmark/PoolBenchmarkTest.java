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
package objectpoolbenchmark;

import java.util.concurrent.*;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.Test;
import stormpot.*;

public class PoolBenchmarkTest {

  private static final Executor executor = Executors.newFixedThreadPool(8);

  @Test
  public void benchmark() throws Exception {
    int size = 16;   // introduce the possibility of a little blocking
    CommonPool storm = new StormPool(size);
    CommonPool basic = new BasicPool(size);
    CommonPool array = new ABQPool(size);

    for (int at = 0; at != 50; ++at) {
      benchmark(storm);
      benchmark(basic);
      benchmark(array);
    }

    long stormN = 0;
    long basicN = 0;
    long arrayN = 0;
    for (int at = 0; at != 30; ++at) {
      stormN += benchmark(storm);
      basicN += benchmark(basic);
      arrayN += benchmark(array);
    }

    System.out.println("storm = " + ((double)(TimeUnit.NANOSECONDS.toMillis(stormN))) / 1000.00 ) ;
    System.out.println("basic = " + ((double)(TimeUnit.NANOSECONDS.toMillis(basicN))) / 1000.00 ) ;
    System.out.println("array = " + ((double)(TimeUnit.NANOSECONDS.toMillis(arrayN))) / 1000.00 ) ;
  }

  private long benchmark(final CommonPool pool) throws Exception {
    final int N = 5000;
    final CountDownLatch latch = new CountDownLatch(N);
    final Runnable runnable = new PooledOperation(pool, latch);

    long start = System.nanoTime();
    for (int at = 0; at != N; ++at) {
      executor.execute(runnable);
    }

    latch.await();

    return System.nanoTime() - start;
  }

  final class PooledOperation implements Runnable {
    private final CommonPool pool;
    private final CountDownLatch latch;

    PooledOperation(CommonPool pool, CountDownLatch latch) {
      this.pool = pool;
      this.latch = latch;
    }

    @Override
    public void run() {
      try {
        // perform two claims
        for (int at = 0; at != 1; ++at) {
          final PooledStringBuilder pooled = pool.acquire();
          operate(pooled.getValue());
          pool.release(pooled);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        System.exit(-1);
      }

      latch.countDown();
    }

    private String operate(StringBuilder sb) {
      for (int at = 0; at != 10; ++at) {
        sb.append("foo").append("bar").append(sb.toString());
        sb.reverse();
      }

      String s = sb.toString();
      sb.setLength(0);
      return s;
    }
  }

  interface CommonPool {
    PooledStringBuilder acquire() throws Exception;
    void release(PooledStringBuilder builder);
  }

  private static class ABQPool implements CommonPool {
    private BlockingQueue<PooledStringBuilder> queue;

    ABQPool(int size) {
      queue = new ArrayBlockingQueue<>(size);
      for (int at = 0; at != size; ++at) {
        queue.add(new PooledStringBuilder());
      }
    }

    @Override
    public PooledStringBuilder acquire() throws Exception {
      return queue.take();
    }

    @Override
    public void release(PooledStringBuilder builder) {
      queue.add(builder);
    }
  }

  private static class BasicPool implements CommonPool, PooledObjectFactory<PooledStringBuilder> {

    private final GenericObjectPool<PooledStringBuilder> pool;

    public BasicPool(int size) {
      pool = new GenericObjectPool<>(this);
      pool.setMaxTotal(size);
    }

    @Override
    public PooledStringBuilder acquire() throws Exception {
      return pool.borrowObject();
    }

    @Override
    public void release(PooledStringBuilder builder) {
      pool.returnObject(builder);
    }

    @Override
    public PooledObject<PooledStringBuilder> makeObject() throws Exception {
      return new DefaultPooledObject<>(new PooledStringBuilder());
    }

    @Override
    public void destroyObject(PooledObject<PooledStringBuilder> p) throws Exception {

    }

    @Override
    public boolean validateObject(PooledObject<PooledStringBuilder> p) {
      return true;
    }

    @Override
    public void activateObject(PooledObject<PooledStringBuilder> p) throws Exception {
    }

    @Override
    public void passivateObject(PooledObject<PooledStringBuilder> p) throws Exception {
    }
  }

  private static class StormPool implements CommonPool, Allocator {
    private final Pool<PooledStringBuilder> pool;
    private final Timeout timeout = new Timeout(1, TimeUnit.MINUTES);

    StormPool(int size) {
      Config<PooledStringBuilder> config = new Config<>();
      config.setSize(size);
      config.setAllocator(this);
      pool = new BlazePool<>(config);
    }

    @Override
    public PooledStringBuilder acquire() throws Exception {
      return pool.claim(timeout);
    }

    @Override
    public void release(PooledStringBuilder builder) {
      builder.release();
    }

    @Override
    public Poolable allocate(Slot slot) throws Exception {
      return new PooledStringBuilder(slot);
    }

    @Override
    public void deallocate(Poolable poolable) throws Exception {
    }
  }

  private static class PooledStringBuilder implements Poolable {
    private final Slot slot;
    private final StringBuilder builder = new StringBuilder();

    PooledStringBuilder() {
      this(null);
    }

    PooledStringBuilder(Slot slot) {
      this.slot = slot;
    }

    public StringBuilder getValue() {
      return builder;
    }

    @Override
    public void release() {
      if (slot != null)

        slot.release(this);
    }
  }
}

