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
package objectpoolbenchmark.suite;

import com.zaxxer.hikari.util.ConcurrentBag;
import nf.fr.eraasoft.pool.PoolSettings;
import nf.fr.eraasoft.pool.impl.PoolControler;
import objectpoolbenchmark.suite.commonspool.MyCommonsObject;
import objectpoolbenchmark.suite.commonspool.MyPoolableObjectFactory;
import objectpoolbenchmark.suite.commonspool2.MyCommons2Object;
import objectpoolbenchmark.suite.commonspool2.MyPooledObjectFactory;
import objectpoolbenchmark.suite.furious.MyFuriousObject;
import objectpoolbenchmark.suite.furious.MyPoolableObject;
import objectpoolbenchmark.suite.stormpot.GenericAllocator;
import objectpoolbenchmark.suite.stormpot.GenericPoolable;
import objectpoolbenchmark.suite.stormpot.GenericPoolableExpiration;
import objectpoolbenchmark.suite.vibur.MyViburObject;
import objectpoolbenchmark.suite.vibur.ViburObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openjdk.jmh.annotations.*;
import org.vibur.objectpool.util.ConcurrentLinkedQueueCollection;
import ru.narod.dimzon541.utils.pooling.EasyPool;
import stormpot.Pool;
import stormpot.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@State(Scope.Benchmark)
public abstract class ClaimRelease
{
  @Param({"10"})
  public int poolSize;

  @Setup
  public abstract void preparePool() throws Exception;

  @TearDown
  public abstract void tearDownPool() throws Exception;

  @CompilerControl(CompilerControl.Mode.INLINE)
  public abstract Object claim() throws Exception;

  @CompilerControl(CompilerControl.Mode.INLINE)
  public abstract void release(Object obj) throws Exception;


  @Benchmark
  public void cycle() throws Exception {
    Object obj = claim();
    release(obj);
  }

  public static class StormpotBlazePool extends ClaimRelease {
    private final Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
    private Pool<GenericPoolable> pool;

    @Override
    public void preparePool() {
      pool = Pool.from(new GenericAllocator()).setSize(poolSize).setExpiration(new GenericPoolableExpiration()).build();
    }

    @Override
    public void tearDownPool() throws InterruptedException {
      pool.shutdown().await(timeout);
    }

    @Override
    public Object claim() throws Exception {
      return pool.claim(timeout);
    }

    @Override
    public void release(Object obj) {
      ((GenericPoolable)obj).release();
    }
  }

  public static class Furious extends ClaimRelease {
    private nf.fr.eraasoft.pool.ObjectPool<MyFuriousObject> pool;

    @Override
    public void preparePool() {
      PoolSettings<MyFuriousObject> settings = new PoolSettings<>(new MyPoolableObject());
      settings.min(0).max(poolSize);
      pool = settings.pool();
    }

    @Override
    public void tearDownPool() {
      PoolControler.shutdown();
    }

    @Override
    public Object claim() throws Exception {
      return pool.getObj();
    }

    @Override
    public void release(Object obj) {
      pool.returnObj((MyFuriousObject) obj);
    }
  }

  public static class CommonsPool2 extends ClaimRelease {
    private org.apache.commons.pool2.ObjectPool<MyCommons2Object> pool;

    @Override
    public void preparePool() {
      GenericObjectPoolConfig<MyCommons2Object> config = new GenericObjectPoolConfig<>();
      config.setMaxTotal(poolSize);
      config.setBlockWhenExhausted(true);
      config.setTestOnBorrow(true);
      pool = new org.apache.commons.pool2.impl.GenericObjectPool<>(
          new MyPooledObjectFactory(), config);
    }

    @Override
    public void tearDownPool() {
      pool.close();
    }

    @Override
    public Object claim() throws Exception {
      return pool.borrowObject();
    }

    @Override
    public void release(Object obj) throws Exception {
      pool.returnObject((MyCommons2Object) obj);
    }
  }

  public abstract static class CommonsPool extends ClaimRelease {
    protected org.apache.commons.pool.ObjectPool<MyCommonsObject> pool;

    @Override
    public void tearDownPool() throws Exception {
      pool.close();
    }

    @Override
    public Object claim() throws Exception {
      return pool.borrowObject();
    }

    @Override
    public void release(Object obj) throws Exception {
      pool.returnObject((MyCommonsObject) obj);
    }
  }

  public static class CommonsPoolGeneric extends CommonsPool {
    @Override
    public void preparePool() {
      pool = new org.apache.commons.pool.impl.GenericObjectPool<>(
          new MyPoolableObjectFactory(),
          poolSize,
          GenericObjectPool.WHEN_EXHAUSTED_BLOCK,
          GenericObjectPool.DEFAULT_MAX_WAIT,
          GenericObjectPool.DEFAULT_MAX_IDLE,
          GenericObjectPool.DEFAULT_MIN_IDLE,
          true, // test on borrow
          false,
          GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,
          GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN,
          GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS,
          false,
          GenericObjectPool.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS,
          GenericObjectPool.DEFAULT_LIFO);
    }
  }

  public static class CommonsPoolStack extends CommonsPool {
    @Override
    public void preparePool() {
      pool = new org.apache.commons.pool.impl.StackObjectPool<>(
          new MyPoolableObjectFactory(),
          poolSize);
    }
  }

  public static class ViburObjectPool extends ClaimRelease {
    private org.vibur.objectpool.ConcurrentPool<MyViburObject> pool;

    @Override
    public void preparePool() {
      ViburObjectFactory factory = new ViburObjectFactory();
      ConcurrentLinkedQueueCollection<MyViburObject> collection = new ConcurrentLinkedQueueCollection<>();
      pool = new org.vibur.objectpool.ConcurrentPool<>(collection, factory, poolSize, poolSize, false);
    }

    @Override
    public void tearDownPool() {
      pool.terminate();
    }

    @Override
    public Object claim() {
      return pool.take();
    }

    @Override
    public void release(Object obj) {
      pool.restore((MyViburObject) obj);
    }
  }

  public static class EasyPoolTest extends ClaimRelease {
    EasyPool<Object> easyPool;

    @Override
    public void preparePool() {
      easyPool = new EasyPool<>(poolSize);
    }

    @Override
    public void tearDownPool() {

    }

    @Override
    public Object claim() throws Exception {
      EasyPool<Object>.PoolSocket socket = easyPool.getSocket();
      Object value = socket.getObject();
      if (value == null) {
        value = new Object();
        socket.setObject(value);
        Costs.expendAllocation();
      } else {
        Costs.expendValidation();
      }
      return socket;
    }

    @Override
    public void release(Object obj) throws Exception {
      ((AutoCloseable) obj).close();
    }
  }

  public static class ConcurrentBag1 extends ClaimRelease {
    ConcurrentBag<ConcurrentBag.IConcurrentBagEntry> bag;

    @Override
    public void preparePool() {
      bag = new ConcurrentBag<>(new ConcurrentBag.IBagStateListener() {
        @Override
        public void addBagItem(int i) {
        }
      });
      for (int i = 0; i < poolSize; i++) {
        Costs.expendAllocation();
        bag.add(new ConcurrentBag.IConcurrentBagEntry() {
          final AtomicInteger state = new AtomicInteger(ConcurrentBag.IConcurrentBagEntry.STATE_NOT_IN_USE);

          @Override
          public boolean compareAndSet(int from, int to) {
            return state.compareAndSet(from, to);
          }

          @Override
          public void setState(int newState) {
            state.set(newState);
          }

          @Override
          public int getState() {
            return state.get();
          }
        });
      }
    }

    @Override
    public void tearDownPool() {

    }

    @Override
    public Object claim() throws Exception {
      final ConcurrentBag.IConcurrentBagEntry borrow = bag.borrow(1, TimeUnit.DAYS);
      Costs.expendValidation();
      return borrow;
    }

    @Override
    public void release(Object obj) {
      bag.requite((ConcurrentBag.IConcurrentBagEntry) obj);
    }
  }
}
