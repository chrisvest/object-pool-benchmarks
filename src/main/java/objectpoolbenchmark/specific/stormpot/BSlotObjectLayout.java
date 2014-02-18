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

import java.lang.reflect.Constructor;
import java.util.concurrent.BlockingQueue;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.util.VMSupport;

public class BSlotObjectLayout {
  public static void main(String[] args) throws Exception {
    Class<?> bslotClass = Class.forName("stormpot.BSlot");
    Constructor<?> slotConstructor = bslotClass.getConstructor(BlockingQueue.class);
    slotConstructor.setAccessible(true);
    Object bslot = slotConstructor.newInstance(new Object[]{null});
    System.out.println(VMSupport.vmDetails());
    System.out.println(ClassLayout.parseClass(bslotClass).toPrintable(bslot));
  }
}
