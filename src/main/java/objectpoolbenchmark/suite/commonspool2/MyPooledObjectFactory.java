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
package objectpoolbenchmark.suite.commonspool2;

import objectpoolbenchmark.suite.Costs;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class MyPooledObjectFactory extends BasePooledObjectFactory<MyCommons2Object> {

  @Override
  public MyCommons2Object create() throws Exception {
    Costs.expendAllocation();
    return new MyCommons2Object();
  }

  @Override
  public PooledObject<MyCommons2Object> wrap(MyCommons2Object obj) {
    return new DefaultPooledObject<>(obj);
  }

  @Override
  public boolean validateObject(PooledObject<MyCommons2Object> p) {
    Costs.expendValidation();
    return true;
  }

  @Override
  public void destroyObject(PooledObject<MyCommons2Object> p) throws Exception {
    Costs.expendDeallocation();
  }
}
