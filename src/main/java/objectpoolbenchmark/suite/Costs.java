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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openjdk.jmh.logic.BlackHole;

public class Costs {
  private static final long allocationCost;
  private static final CostUnit allocationCostUnit;
  private static final long deallocationCost;
  private static final CostUnit deallocationCostUnit;
  private static final long validationCost;
  private static final CostUnit validationCostUnit;

  static {
    Cost cost;
    cost = Cost.getCost("cost.of.allocation");
    if (cost != null) {
      allocationCost = cost.value;
      allocationCostUnit = cost.unit;
    } else {
      allocationCost = 0;
      allocationCostUnit = null;
    }

    cost = Cost.getCost("cost.of.deallocation");
    if (cost != null) {
      deallocationCost = cost.value;
      deallocationCostUnit = cost.unit;
    } else {
      deallocationCost = 0;
      deallocationCostUnit = null;
    }

    cost = Cost.getCost("cost.of.validation");
    if (cost != null) {
      validationCost = cost.value;
      validationCostUnit = cost.unit;
    } else {
      validationCost = 0;
      validationCostUnit = null;
    }
  }

  public static void expendAllocation() {
    if (allocationCost > 0) {
      allocationCostUnit.spend(allocationCost);
    }
  }

  public static void expendDeallocation() {
    if (deallocationCost > 0) {
      deallocationCostUnit.spend(deallocationCost);
    }
  }

  public static void expendValidation() {
    if (validationCost > 0) {
      validationCostUnit.spend(validationCost);
    }
  }

  private static class Cost {
    private static final Pattern format = Pattern.compile("(\\d+)(\\w+)");
    public long value;
    public CostUnit unit;

    public static Cost getCost(String propertyName) {
      String unparsed = System.getProperty(propertyName);
      if (unparsed == null || unparsed.trim().length() == 0) {
        return null;
      }

      Matcher matcher = format.matcher(unparsed);
      if (matcher.find()) {
        String valueStr = matcher.group(1);
        String unitStr = matcher.group(2).toLowerCase();
        CostUnit unit;
        switch (unitStr) {
          case "cpu": unit = CostUnit.CPU; break;
          case "ms": unit = CostUnit.SLEEP; break;
          default: throw new AssertionError(
              "Unknown unit '" + unitStr + "' in '" + unparsed + "' for " + propertyName);
        }
        Cost cost = new Cost();
        cost.value = Long.parseLong(valueStr);
        cost.unit = unit;
        return cost;
      }

      return null;
    }
  }

  private static enum CostUnit {
    CPU {
      @Override
      public void spend(long units) {
        BlackHole.consumeCPU(units);
      }
    },
    SLEEP {
      @Override
      public void spend(long units) {
        try {
          Thread.sleep(units);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    };

    public abstract void spend(long units);
  }
}
