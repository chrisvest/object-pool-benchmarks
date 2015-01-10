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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
public class HashMapGet {

  @Param({"1000"})
  public int mapSize;

  private Map<Integer, Integer> map;

  @Setup
  public void setUp() {
    map = new HashMap<>();
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < mapSize; i++) {
      map.put(i, random.nextInt());
    }
  }

  @Benchmark
  public int get() {
    return map.get(ThreadLocalRandom.current().nextInt(mapSize));
  }

}
