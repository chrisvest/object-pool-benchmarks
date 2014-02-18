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
