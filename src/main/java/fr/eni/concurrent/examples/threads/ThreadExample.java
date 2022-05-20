package fr.eni.concurrent.examples.threads;


import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by ljoyeux on 27/04/2017.
 */
public class ThreadExample {

    public static class ThreadTimeoutWait extends Thread {
        boolean timeout;

        public synchronized void doWait(long timeout) throws InterruptedException {
            this.timeout = true;
            super.wait(timeout);
            System.out.println("is timeout: " + this.timeout);
        }

        public boolean isTimeout() {
            return timeout;
        }

        public synchronized void doNotify() {
            this.timeout = false;
            notify();
        }
    }

    public static class ThreadTimeoutWaitV2 extends Thread {
        boolean timeout;

        public void doWait(long timeout) throws InterruptedException {
            waitOrNotify(timeout);
        }

        public boolean isTimeout() {
            return timeout;
        }

        public void doNotify() {
            try {
                waitOrNotify(null);
            } catch (InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
        }

        private synchronized void waitOrNotify(Long timeout) throws InterruptedException {
            if(timeout!=null) {
                this.timeout = true;
                super.wait(timeout);
                System.out.println("is timeout: " + this.timeout + " state " + this.getState());
            } else {
                this.timeout = false;
                notify();
                sleep(500);
                System.out.println("state " + this.getState());
                sleep(500);
            }
        }
    }

    public static void timeoutWait() {
        System.out.println("timeoutWait");
        ThreadTimeoutWaitV2 thread = new ThreadTimeoutWaitV2(){
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        doWait(1000);
                        if(!isTimeout()) {
                            throw new IllegalStateException("Timeout expected");
                        }
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        };

        thread.start();
        try {
            Thread.sleep(2000);
            thread.doNotify();
            thread.join();
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }


    public static void sleep(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static class ThreadSync extends Thread {
        private Object obj = new Object() {
            @Override
            public void finalize() throws Throwable {
                System.out.println("Lock object finalized");
                super.finalize();
            }
        };

        @Override
        public void finalize() throws Throwable {
            System.out.println("Thread Finalised");
            super.finalize();
        }

        @Override
        public void run() {
            synchronized (obj) {
                try {
                    obj.wait();
                    System.out.println("Object in child thread : " + obj);
                } catch (InterruptedException ex) {
                }
            }
        }

        public void nullifyObject() {
            obj = null;
        }

        public Object getObj() {
            return obj;
        }
    }

    public static void threadSyncNoFinal() {
        ThreadSync thread = new ThreadSync();
        thread.start();

        sleep(1000);
        Object obj = thread.getObj();
        thread.nullifyObject();

        synchronized (obj) {
            obj.notify();
        }

        try {
            thread.join();
        } catch (InterruptedException ex) {
        }

        System.out.println("Thread terminated");
        thread = null;

        System.gc();
        sleep(2000);
        System.runFinalization();
        sleep(2000);
    }

    public static void threadSyncNoFinalGC() {
        ThreadSync thread = new ThreadSync();
        thread.start();

        sleep(1000);
        thread.nullifyObject();

        System.out.println("Performing GC");
        System.gc();

        try {
            thread.join(2000);
        } catch (InterruptedException ex) {
        }

        System.out.println("Thread state : " + thread.getState());

        System.out.println("Thread terminated");
    }

    public static synchronized void syncRecursive(int level) {
        System.out.println("Enter in syncRecursive(" + level + ")");

        if(level>=0 ) {
            if (level < 5) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }

                syncRecursive(level + 1);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            } else {
                new Thread() {
                    @Override
                    public void run() {
                        syncRecursive(-1);
                    }
                }.start();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }

        System.out.println("Leaving syncRecursive(" + level + ")");
    }

    public static void noTimeoutWait() {
        System.out.println("noTimeoutWait");
        ThreadTimeoutWaitV2 thread = new ThreadTimeoutWaitV2(){
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        doWait(2000);
                        if(isTimeout()) {
                            throw new IllegalStateException("Timeout not expected");
                        }
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        };

        thread.start();
        try {
            Thread.sleep(1000);
            thread.doNotify();
            thread.join();
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    static abstract class IndexRunnable implements Runnable {
        private final int index;

        public IndexRunnable(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public static void multipleWait() {
        final Object obj = new Object();

        final List<Thread> threads = new ArrayList<>();
        for(int i=0; i<5; i++) {
            Runnable run = new IndexRunnable(i) {
                public void run() {
                    synchronized (obj) {
                        try {
                            System.out.println("Waiting in thread " + getIndex());
                            obj.wait();
                            System.out.println("Resuming thread " + getIndex());
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            };

            Thread thread = new Thread(run);
            thread.start();
            threads.add(thread);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }

        System.out.println("Notify All");

        synchronized (obj) {
            obj.notifyAll();
        }

        for(Thread th: threads) {
            try {
                th.join();
            } catch (InterruptedException ex) {
            }
        }

        System.out.println("All threads are terminated");
    }

    public static void multipleWaitNotify() {
        final Object obj = new Object();

        final List<Thread> threads = new ArrayList<>();
        for(int i=0; i<5; i++) {
            Runnable run = new IndexRunnable(i) {
                public void run() {
                    synchronized (obj) {
                        try {
                            System.out.println("Waiting in thread " + getIndex());
                            obj.wait();
                            System.out.println("Resuming thread " + getIndex());
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            };

            Thread thread = new Thread(run);
            thread.start();
            threads.add(thread);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }

        System.out.println("Notify several times");

        for(int i=0; i<threads.size(); i++) {
            synchronized (obj) {
                obj.notify();
            }
        }

        for(Thread th: threads) {
            try {
                th.join();
                if(th.getState().equals(Thread.State.RUNNABLE)) {
                    System.out.println("This thread is running");
                }
            } catch (InterruptedException ex) {
            }
        }

        System.out.println("All threads are terminated");
    }

    public static void multipleWaitUnderNotify() {
        final Object obj = new Object();

        final List<Thread> threads = new ArrayList<>();
        for(int i=0; i<5; i++) {
            Runnable run = new IndexRunnable(i) {
                public void run() {
                    synchronized (obj) {
                        try {
                            System.out.println("Waiting in thread " + getIndex());
                            obj.wait();
                            System.out.println("Resuming thread " + getIndex());
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            };

            Thread thread = new Thread(run);
            thread.start();
            threads.add(thread);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }

        System.out.println("Notify several times");

        for(int i=0; i<threads.size()-1; i++) {
            synchronized (obj) {
                obj.notify();
            }
        }

        int numAliveThreads = 0;
        for(Thread th: threads) {
            try {
                th.join(2000);

                if(!th.getState().equals(Thread.State.TERMINATED)) {
                    numAliveThreads++;
                }
            } catch (InterruptedException ex) {
            }
        }

        System.out.println(numAliveThreads!=0 ? numAliveThreads + " thread(s) are note terminated" : "All threads are terminated");

        while(numAliveThreads>0) {
            synchronized (obj) {
                obj.notify();
            }
            numAliveThreads--;
        }
    }

    public static void threadList() {
        for(int i=0; i<5; i++) {
            new Thread() {
                @Override
                public void run() {
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }.start();
        }

        sleep(1000);
        int nbThreads = Thread.activeCount();
        System.out.println("number of threads : " + nbThreads);
        Thread[] threads = new Thread[nbThreads];
        Thread.enumerate(threads);

        for(Thread th: threads) {
            System.out.println("Thread id: " + th.getId() + ", thread state : " + th.getState() + " " + th);
        }

        System.out.println("Current thread " + Thread.currentThread().getId()) ;

        for(final Thread th: threads) {
            if(th.getState().equals(Thread.State.WAITING)) {
                synchronized (th) {
                    th.notify();
                }
            }
        }
    }

    public static void threadPriority() {
        Thread thread = new Thread() {
            @Override
            public void run() {

            }
        };

        System.out.println("Min priority : " + Thread.MIN_PRIORITY);
        System.out.println("max priority: " + Thread.MAX_PRIORITY);
        System.out.println("normal priority: " + Thread.NORM_PRIORITY);

        thread.setPriority(Thread.MAX_PRIORITY);
    }

    public static void threadGroup() {
        Thread thread = Thread.currentThread();
        System.out.println("Current thread: " + thread.toString());

        ThreadGroup threadGroup = thread.getThreadGroup();
        System.out.println("ThreadGroup: " + threadGroup);

        Thread th = new Thread();
        System.out.println(th.getThreadGroup());
        System.out.println(th);

        ThreadGroup group = new ThreadGroup(threadGroup, "my group");

        Thread th1 = new Thread(group, new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex){
                }
            }
        });

        System.out.println("active threads in \"" + group.getName() + "\": " + group.activeCount());
        System.out.println("Start thread");
        th1.start();
        System.out.println("active threads in \"" + group.getName() + "\": " + group.activeCount());


        System.out.println(th1);
    }

    public static class OjectWithSyncMethod {
        public synchronized void method() {

        }
    }

    public static OjectWithSyncMethod syncMethod() {
        final OjectWithSyncMethod obj = new OjectWithSyncMethod();
        for(int i=0; i<1_000_000; i++) {
            obj.method(); // synchronized method
        }
        return obj;
    }

    public static class ObjectWithSyncMethods {
        public synchronized void method1() {
            // code
        }

        public synchronized void method2() {
            // code
        }
    }

    public static void syncMethodCalls() {
        final ObjectWithSyncMethods obj = new ObjectWithSyncMethods();


        obj.method1();
        // in between code
        obj.method2();

        synchronized (obj) {
            obj.method1();
            obj.method2();
        }
    }

    public static class FinalizedObject {
        @Override
        protected void finalize() throws Throwable {
            System.out.println("finalized");
        }
    }

    public static void threadLocalOnJoinThread() throws InterruptedException {
        final ThreadLocal<Object> locals = new ThreadLocal<>();

        Thread th = new Thread() {
            @Override
            public void run() {
                locals.set(new FinalizedObject());
            }
        };

        th.start();
        th.join();

        System.gc();
        Thread.sleep(1000);
    }

    public static class ThreadVar {
        public static Long classFieldId; // classe field

        public Long instanceFieldId; // instance field
    }

    final static ThreadLocal<Integer> ids = new ThreadLocal<>();
    final static Map<Thread, Integer> idsInThreads = new HashMap<>();

    static int running = 0;
    final static Object sync = new Object();

    public static void threadLocal() {
        final Random r = new Random(System.nanoTime());
        final List<Thread> threads = new ArrayList<>();

        for(int i=0; i<5; i++) {
            final int j = i;
            Thread th = new Thread() {
                final int threadNumber = j;

                @Override
                public void run() {
                    ids.set(0);

                    synchronized (idsInThreads) {
                        idsInThreads.put(this, 0);
                    }

                    synchronized (sync) {
                        try {
                            running++;
                            sync.wait();
                        } catch (InterruptedException ex){
                        }
                    }

                    for (; ; ) {
                        Integer id = ids.get();
                        if (id++ == 5) {
                            System.out.println("Stop thread " + threadNumber);
                            return;
                        }

                        synchronized (idsInThreads) {
                            idsInThreads.put(this, id);
                            System.out.print("Value for thread number " + threadNumber + ": " + id + ". In others threads: ");
                            for(int j=0; j<threads.size(); j++) {
                                if(j>0) {
                                    System.out.print(", ");
                                }
                                System.out.print(j  + " ->  " + idsInThreads.get(threads.get(j)));
                            }
                            System.out.println();
                        }

                        ids.set(id);

                        try {
                            Thread.sleep(100 + r.nextInt(700));
                        } catch (InterruptedException ex) {
                        }
                    }

                }
            };

            th.start();
            threads.add(th);
        }

        for(;;) {
            synchronized (sync) {
                if(running==threads.size()) {
                    break;
                }
            }
        }

        synchronized (sync) {
            sync.notifyAll();
        }

        for(Thread th: threads) {
            try {
                th.join();
            } catch (InterruptedException ex) {
            }
        }
    }

    public static void badUnlockBlock() throws Exception {
        Lock lock = new ReentrantLock();
        Random r = new Random(System.nanoTime());

        lock.lock();

        if(r.nextInt(2)==1) {
            return; // no unlock
        }

        if(r.nextInt(2)==1) {
            throw  new Exception(); // no unlock
        }

        lock.unlock();
    }

    public static void goodUnlockBlock() throws Exception {
        Lock lock = new ReentrantLock();
        Random r = new Random(System.nanoTime());
        try {
            lock.lock();

            if (r.nextInt(2) == 1) {
                return; // no unlock
            }

            if (r.nextInt(2) == 1) {
                throw new Exception(); // no unlock
            }
        } finally {
            lock.unlock();
        }
    }

    static Lock lock = new ReentrantLock();

    public static void reentrantLock(int level) {
        try {
            System.out.println("Enter in reentrantLock(" + level + ")");

            lock.lock();
            if (level >= 0) {
                if (level < 5) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }

                    reentrantLock(level + 1);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            boolean wasLocked = false;
                            try {
                                wasLocked = lock.tryLock();
                                System.out.println("Can lock in thread child: " + wasLocked);
                            } finally {
                                if(wasLocked) {
                                    lock.unlock();
                                }
                            }
                            reentrantLock(-1);
                        }
                    }.start();

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }

        } finally {
            System.out.println("Leaving reentrantLock(" + level + ")");
            lock.unlock();
        }
    }

    public static void lockRessource() {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final PrintWriter writer = new PrintWriter(os);
        for(int i=1; i<=20; i++) {
            writer.println("line " + i);
        }

        writer.flush();

        final Lock lock = new ReentrantLock();

        final List<Thread> threads = new ArrayList<>();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(os.toByteArray())));

        final Random r = new Random(System.nanoTime());

        final ByteArrayOutputStream outOS = new ByteArrayOutputStream();
        final PrintWriter outWriter = new PrintWriter(outOS);
        for(int i=0; i<5; i++) {
            Thread thread = new Thread(Integer.toString(i)) {
                @Override
                public void run() {

                    final MessageDigest digest;

                    try {
                        digest = MessageDigest.getInstance("MD5");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        return;
                    }

                    for(;;) {
                        String str = null;
                        lock.lock();
                        try {
                            str = reader.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            lock.unlock();
                        }

                        if (str==null) {
                            System.out.println("Thread " + getName() + " Leaving");
                            return;
                        }

                        final byte[] md5 = digest.digest(str.getBytes());

                        final StringBuilder md5Hexa = new StringBuilder();
                        for(byte b: md5) {
                            md5Hexa.append(String.format("%02X", b&0xff));
                        }

                        System.out.println( "Thread " + getName() + " read : " + str  + " -> " + md5Hexa);
                        try {
                            sleep(100 + r.nextInt(2000));
                        } catch (InterruptedException ex) {
                        }

                        try {
                            lock.lock();
                            outWriter.println(str + " -> " + md5Hexa);
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            };
            threads.add(thread);
        }

        for(Thread thread: threads) {
            thread.start();
        }

        for(Thread thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
            }
        }

        outWriter.flush();
        System.out.println(outOS.toString());
    }

    static DetailReentrantLock reentrantLock = new DetailReentrantLock();
    static List<Thread> threads = new ArrayList<>();

    static class DetailReentrantLock extends ReentrantLock {
        @Override
        public Thread getOwner() {
            return super.getOwner();
        }

        @Override
        public Collection<Thread> getQueuedThreads() {
            return super.getQueuedThreads();
        }
    }

    public static void reentrantLockDetails(int level) {
        try {
            System.out.println("Enter in reentrantLock(" + level + ")");
            reentrantLock.lock();
            System.out.println("holdCount: " + reentrantLock.getHoldCount());
            if (level >= 0) {
                if (level < 3) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }

                    reentrantLockDetails(level + 1);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                } else {
                    Thread thread = new Thread("Child") {
                        @Override
                        public void run() {
                            System.out.println("In Child Thread");
                            System.out.println("\tisHeldByCurrentThread: " + reentrantLock.isHeldByCurrentThread());
                            System.out.println("\tisLocked: " + reentrantLock.isLocked());
                            reentrantLockDetails(-1);
                        }
                    };

                    threads.add(thread);
                    thread.start();

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }

        } finally {
            for(Thread th: threads) {
                System.out.println(th.getName() + " thread in thread queue: " + reentrantLock.hasQueuedThread(th));
            }
            System.out.println("Queue length: " + reentrantLock.getQueueLength());
            System.out.println("Leaving reentrantLock(" + level + ")");

            System.out.println("Owner : " + reentrantLock.getOwner().getName());

            System.out.println("Queued threads: ");
            for (Thread th: reentrantLock.getQueuedThreads()) {
                System.out.print(th.getName() + " ");
            }

            System.out.println();

            reentrantLock.unlock();
        }
    }

    public static void reentrantRW() {

        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

        try {
            readLock.lock();
            // read input stream
        } finally {
            readLock.unlock();
        }

        // process data

        try {
            writeLock.lock();
            // write output stream
        } finally {
            writeLock.unlock();
        }
    }

    public static void lockCondition() {
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();

        final List<Thread> threads = new ArrayList<>();
        for(int i=0; i<5; i++) {
            Thread thread = new Thread(Integer.toString(i)) {
                @Override
                public void run() {
                    System.out.println("Wait for condition in thread " + getName());
                    try {
                        lock.lock();
                        condition.awaitUninterruptibly();
                    } finally {
                        lock.unlock();
                    }

                    System.out.println("Condition signaled in thread " + getName());

                }
            };
            threads.add(thread);
            thread.start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        try {
//            lock.lock();
            condition.signalAll();
        } finally {
//            lock.unlock();
        }

        for(Thread th: threads) {
            try {
                th.join();
            } catch (InterruptedException e) {
            }
        }
    }

    public static void fairLock() throws InterruptedException {
        final Lock lock = new ReentrantLock(true);

        Thread longWaitingThread = new Thread() {
            @Override
            public void run() {
                System.out.println("Long wait thread");
                try {
                    lock.lock();
                } finally {
                    lock.unlock();
                }
                System.out.println("Leaving long wait thread");
            }
        };

        Thread shortWaitingThread = new Thread() {
            @Override
            public void run() {
                System.out.println("Short wait thread");
                try {
                    lock.lock();
                } finally {
                    lock.unlock();
                }
                System.out.println("Leaving short wait thread");
            }
        };

        lock.lock();
        longWaitingThread.start();
        sleep(1000);
        shortWaitingThread.start();
        sleep(1000);
        lock.unlock();

        longWaitingThread.join();
        shortWaitingThread.join();
    }

    public static abstract class SyncAsync {
        public abstract List<String> syncCall(int i);
        public abstract Future<List<String>> asyncCall(int i);

    }


    public static void synchronizedColllections() {

        // List
        List<String> list = new ArrayList<>(); // not synchronized version
        List<String> syncList = Collections.synchronizedList(list); // synchronized version
        syncList.add("bonjour");
        System.out.println(list);

        // Map
        Map<String, String> map = new LinkedHashMap<>();
        Map<String, String> syncMap = Collections.synchronizedMap(map);

        syncMap.put("firstName", "Laurent");
        syncMap.put("lastName", "Joyeux");
        System.out.println(map);

        // Set
        Set<Integer> set = new LinkedHashSet<>();
        Set<Integer> syncSet = Collections.synchronizedSet(set);

        syncSet.add(2);
        syncSet.add(1);
        syncSet.add(1);
        System.out.println(set);

        // bad synchronisation
        Collection<String> collection = Collections.synchronizedCollection(list);
        list.get(0); // ok
//        collection.get(0); // get is not available



        // long synchronisation
        // on non synchronized collection
        synchronized (list) {
            list.clear();
            list.add("first element");
            list.add("second element");
        }

        // on synchronized collection
        synchronized (syncList) {
            syncList.clear();
            syncList.add("first element");
            syncList.add("second element");
        }
    }

    public static void swing() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // code executed by the main thread
            }
        });

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    // code executed by the main thread
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
        }
    }

    public static class Mutex {
        boolean lock;

        public void acquire() {
            perform(true);
        }

        public void release() {
            perform(false);
        }

        private synchronized void perform(boolean acquire) {
            if(acquire) {
                if(!lock) {
                    lock = true;
                } else {
                    try {
                        wait();
                        lock = true;
                    } catch (InterruptedException ex) {
                    }
                }
            } else {
                if(lock) {
                    lock = false;
                    notify();
                }
            }
        }
    }

    public static void threadCycle() throws InterruptedException {
        long begin = System.currentTimeMillis();
        for(int i=0; i<100_0000; i++) {
            Thread thread = new Thread();
            thread.start();
            thread.join();
        }
        long end = System.currentTimeMillis();
        System.out.println("Time to create and destroy 100000 threads: " + (end-begin)*0.001);

        final Mutex mutex = new Mutex();
        mutex.acquire();

        Thread thread = new Thread() {
            @Override
            public void run() {
                for(;;) {
                    mutex.acquire();
                    if(isInterrupted()) {
                        return;
                    }
                    mutex.release();
                }
            }
        };

        thread.start();
        Thread.sleep(1_000);

        begin = System.currentTimeMillis();

        for(int i=0; i<100_0000; i++) {
            mutex.release();
            mutex.acquire();
        }

        end = System.currentTimeMillis();
        System.out.println("Time 100000 mutex: " + (end-begin)*0.001);
        thread.interrupt();
        mutex.release();
        thread.join();
    }

    public static void asyncForkJoinTask() throws InterruptedException {
        final Random r = new Random(System.currentTimeMillis());
        ForkJoinPool pool = new ForkJoinPool();
        for(int i=0; i<20; i++) {
            final int index = i;
            ForkJoinTask<?> task = ForkJoinTask.adapt(new Runnable() {
                @Override
                public void run() {
                    int waitTime = r.nextInt(1000);
                    try {
                        Thread.sleep(200 + waitTime);
                    System.out.println("running " + index);
                    } catch (InterruptedException ex ) {
                    }
                }
            });
            pool.execute(task);
        }

        System.out.println(pool);
        for(;;) {
            System.out.println(pool + " " + pool.isQuiescent());
            Thread.sleep(1000);
            pool.shutdownNow();
            if(pool.isQuiescent()) {
                break;
            }
        }
    }

    public static class Combination extends RecursiveTask<Long> {

        private final int n;
        private final int p;

        public Combination(int n, int p) {
            this.n = n;
            this.p = p;
        }

        @Override
        protected Long compute() {
            if(p>n || p<0) {
                return 0L;
            } else if(n==1) {
                return 1L;
            } else if(n==2) {
                if(p==1) {
                    return 2L;
                }  else {
                    return 1L;
                }
            } else {
                Combination up = new Combination(n - 1, p);
                up.fork();
                Combination upLeft = new Combination(n-1, p-1);
                return upLeft.compute() + up.join();
            }
        }
    }

    public static final class ParallelSort extends RecursiveAction {

        private static final int THRESHOLD = 1024*1024;

        private final double[] array;
        private final int begin;
        private final int end;

        private double[] tmp;

        public ParallelSort(double[] array) {
            this.array = array;
            begin = 0;
            end = array.length;
        }

        public ParallelSort(double[] array, int begin, int end) {
            this.array = array;
            this.begin = begin;
            this.end = end;
        }

        private ParallelSort(double[] array, int begin, int end, double[] tmp) {
            this.array = array;
            this.begin = begin;
            this.end = end;
            this.tmp = tmp;
        }

        @Override
        protected void compute() {
            if(end-begin > THRESHOLD) {
                int middle = (end+begin)/2;

                if(tmp==null) {
                    tmp = new double[array.length];
                }

                ParallelSort leftSort = new ParallelSort(array, begin, middle, tmp);
                ParallelSort rightSort = new ParallelSort(array, middle, end, tmp);

                invokeAll(leftSort, rightSort);

                // merge
                int leftIndex = begin;
                int rightIndex = middle;
                for(int i=begin; i<end; i++) {
                    if(leftIndex==middle) {
                        tmp[i] = array[rightIndex++];
                    } else if(rightIndex==end) {
                        tmp[i] = array[leftIndex++];
                    } else {

                        double leftValue = array[leftIndex];
                        double rightValue = array[rightIndex];
                        if (leftValue < rightValue) {
                            tmp[i] = leftValue;
                            leftIndex++;
                        } else {
                            tmp[i] = rightValue;
                            rightIndex++;
                        }
                    }
                }

                System.arraycopy(tmp, begin, array, begin, end - begin);
            } else {
                    Arrays.sort(array, begin, end);
            }
        }
    }


    public static void syncForkTask() {
        final Random r = new Random(System.currentTimeMillis());
        List<ForkJoinTask<?>> tasks = new ArrayList<>();
        for(int i=0; i<20; i++) {
            final int index = i;
            ForkJoinTask<?> task = ForkJoinTask.adapt(new Runnable() {
                @Override
                public void run() {
                    int waitTime = r.nextInt(1000);
                    try {
                        Thread.sleep(200 + waitTime);
                        System.out.println("running " + index);
                    } catch (InterruptedException ex ) {
                    }
                }
            });
            tasks.add(task);
        }

        System.out.println("Start");
        ForkJoinTask.invokeAll(tasks);
        System.out.println("End");
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
//        timeoutWait();
//        noTimeoutWait();
//        syncRecursive(0);
//        Thread.sleep(1000);

//        multipleWaitUnderNotify();
//        threadSyncNoFinal();
//        threadList();
//        threadPriority();
//        final Object obj1 = new Object();
//        final Object obj2 = new Object();
//
//        synchronized (obj1) {
//            synchronized (obj2) {
//                // code ...
//            }
//        }

//        threadGroup();
//
//        Map<Thread, Object> threadContext = new HashMap<>();
//
//        ThreadLocal<Long> ids;

//        new FinalizedObject();

//        threadLocalOnJoinThread();
//        threadLocal();
//
//        Lock lock;

//        reentrantLock(0);
//        lockRessource();

//        threads.add(Thread.currentThread());
//        reentrantLockDetails(0);

//        lockCondition();
//        fairLock();

/*
        SyncAsync syncAsync = new SyncAsync() {
            @Override
            public List<String> syncCall(int n) {
                final List<String> list = new ArrayList<>();
                for(int i=0; i<n; i++) {
                    list.add(Integer.toString(i));
                }

                return list;
            }

            @Override
            public Future<List<String>> asyncCall(int n) {
                final AsyncExec asyncExec = new AsyncExec(n);
                synchronized (asyncExec) {
                    asyncExec.start();
                    try {
                        asyncExec.wait();
                    } catch (InterruptedException ex) {
                    }
                }
                return asyncExec;
            }

            class AsyncExec extends Thread implements Future<List<String>> {
                private final int n;
                private final List<String> list;
                private int i;
                private boolean stop;
                private Lock lock;

                public AsyncExec(int n) {
                    this.n = n;
                    list = new ArrayList<>();
                    lock = new ReentrantLock();
                }

                @Override
                public void run() {
                    synchronized (this) {
                        notify();
                    }

                    try {
                        lock.lock();
                        for (;;) {
                            list.add(Integer.toString(i));
                            synchronized (this) {
                                if (i++ == n || stop) {
                                    return;
                                }
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                }

                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    stop = true;
                    return true;
                }

                @Override
                public boolean isCancelled() {
                    return stop && isDone();
                }

                @Override
                public boolean isDone() {
                    return i==n;
                }

                @Override
                public List<String> get() throws InterruptedException, ExecutionException {
                    try {
                        lock.lock();
                        return list;
                    } finally {
                        lock.unlock();
                    }
                }

                @Override
                public List<String> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    boolean locked = false;
                    try {
                        locked = lock.tryLock(timeout, unit);
                        if (!locked) {
                            throw new TimeoutException();
                        }

                        return list;

                    } finally {
                        if(locked) {
                            lock.unlock();
                        }
                    }
                }
            }
        };

        Future<List<String>> list = syncAsync.asyncCall(200);
        System.out.println(list.get());
*/

//        synchronizedColllections();
//        threadCycle();

//        asyncForkJoinTask();
//        syncForkTask();

//        ForkJoinPool pool = new ForkJoinPool();
//        Combination combination = new Combination(49, 5);
//        ForkJoinTask<Long> computePool = pool.submit(combination);
//        Long invoke = computePool.invoke();
//        System.out.println(invoke);


        Random r = new Random(2773996255688713756L);
        double[] elements = new double[16];
        double[] elementsForParallelSort = new double[elements.length];

        for(int i=0; i<elements.length; i++) {
            elements[i] = r.nextDouble();
        }

        System.arraycopy(elements, 0, elementsForParallelSort, 0, elementsForParallelSort.length);

        long begin = System.currentTimeMillis();
        Arrays.sort(elements, 0, elements.length);
        long end = System.currentTimeMillis();
        System.out.println("Sorted in " + (end-begin) + " milli seconds");

        begin = System.currentTimeMillis();
//        ForkJoinPool pool = new ForkJoinPool();
//        ParallelSort parallelSort = new ParallelSort(elementsForParallelSort);
//        pool.invoke(parallelSort);

//        elementsForParallelSort = CCSort.sort(elementsForParallelSort);

        end = System.currentTimeMillis();

        System.out.println("Sorted in " + (end-begin) + " milli seconds");

        for(int i=0; i<elements.length; i++) {
            if(Math.abs(elements[i]-elementsForParallelSort[i])>1e-10) {
                System.err.println("Elements mismatch");
                break;
            }
        }


    }
}
