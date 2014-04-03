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
