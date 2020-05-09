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
package objectpoolbenchmark.suite.vibur;

import objectpoolbenchmark.suite.Costs;
import org.vibur.objectpool.PoolObjectFactory;

public class ViburObjectFactory implements PoolObjectFactory<MyViburObject> {
  @Override
  public MyViburObject create() {
    Costs.expendAllocation();
    return new MyViburObject();
  }

  @Override
  public boolean readyToTake(MyViburObject obj) {
    Costs.expendValidation();
    return true;
  }

  @Override
  public boolean readyToRestore(MyViburObject obj) {
    return true;
  }

  @Override
  public void destroy(MyViburObject obj) {
    Costs.expendDeallocation();
  }
}
