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
package objectpoolbenchmark.suite.commonspool2;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class MyPooledObjectFactory extends BasePooledObjectFactory<MyCommons2Object> {
  private final long ttlMillis;

  public MyPooledObjectFactory(long ttlMillis) {

    this.ttlMillis = ttlMillis;
  }

  @Override
  public MyCommons2Object create() throws Exception {
    return new MyCommons2Object();
  }

  @Override
  public PooledObject<MyCommons2Object> wrap(MyCommons2Object obj) {
    return new DefaultPooledObject<>(obj);
  }

  @Override
  public boolean validateObject(PooledObject<MyCommons2Object> p) {
    return System.currentTimeMillis() <= p.getCreateTime() + ttlMillis;
  }
}
