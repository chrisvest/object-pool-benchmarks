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
package ru.narod.dimzon541.utils.pooling;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class EasyPool<E> {
    private final ConcurrentLinkedQueue<PoolSocket> sockets = new ConcurrentLinkedQueue<>();
    private final Semaphore semaphore = new Semaphore(1,true);

    public EasyPool(int poolSize) {
        for (int i = 0; i < poolSize; i++) {
            sockets.offer(new PoolSocket());
        }
    }

    public PoolSocket getSocket() throws InterruptedException {
        for(;;){
            PoolSocket socket = sockets.poll();
            if(socket!=null) return socket;
            semaphore.acquire();
        }
    }
    public class PoolSocket implements AutoCloseable{
        private E object;

        public E getObject() {
            return object;
        }

        public void setObject(E object) {
            this.object = object;
        }

        @Override
        public void close() {
            sockets.offer(this);
            semaphore.release();
        }
    }
}
