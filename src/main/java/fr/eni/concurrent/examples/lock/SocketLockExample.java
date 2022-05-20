package fr.eni.concurrent.examples.lock;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ljoyeux on 24/06/2017.
 */
public class SocketLockExample {
    public static final int port = 12357;

    // Commands for lock and unlock requests
    public static final byte LOCK_REQUEST = 0x00;
    public static final byte LOCK_OK = 0x01;

    public static final byte UNLOCK_REQUEST = 0x02;
    public static final byte UNLOCK_OK = 0x021;

    public static final Logger LOGGER = Logger.getLogger(SocketThread.class.getName());

    /**
     * Server used to lock and unlock using a socket
     */
    public static class SocketThread extends Thread {

        private final List<SocketChannel> sockets = new ArrayList<>();
        private final Selector selector;
        private final ByteBuffer buffer = ByteBuffer.allocate(1);

        private final List<SocketChannel> locked = new ArrayList<>();
        private SocketChannel lockedBy;
        private int lockedCounter;

        public SocketThread() throws IOException {
            // Setup a socket server using NIO (non blocking API)

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
            serverSocketChannel.configureBlocking(false);
            int validOps = serverSocketChannel.validOps();
            selector = Selector.open();
            serverSocketChannel.register(selector, validOps, null);
        }

        @Override
        public void run() {
            try {
                for (; !isInterrupted(); ) {

                    // wait for an event (new connection, read or write on a socket)
                    int select = selector.select();
                    if (select<1) {
                        continue;
                    }

                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while(iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        if(key.isAcceptable()) {

                            // new connection

                            LOGGER.log(Level.INFO, "accept");
                            ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
                            SocketChannel channel = serverSocket.accept();

                            LOGGER.log(Level.INFO, "add accept");
                            sockets.add(channel);
                            channel.configureBlocking(false);
                            SelectionKey register = channel.register(selector, SelectionKey.OP_READ);
                            register.attach(sockets.size()-1);
                        } else if(key.isReadable()) {

                            // read operation.

                            LOGGER.log(Level.INFO, "read");
                            SocketChannel channel = (SocketChannel) key.channel();

                            buffer.rewind();
                            int read = channel.read(buffer);
                            LOGGER.log(Level.INFO, "read " + read);
                            if(read==-1) {

                                // connection was closed. Remove it
                                sockets.remove(channel);
                                channel.close();
                            } else if(read>=1) {

                                // Valid read operation with data

                                buffer.rewind();
                                byte op = buffer.get();

                                // command
                                switch (op) {
                                    case LOCK_REQUEST: // lock command
                                        LOGGER.log(Level.INFO, "lock cmd");
                                        if (lockedBy==null || lockedBy.equals(channel)) {
                                            if(channel.equals(lockedBy)) {
                                                // reentrant lock
                                                lockedCounter++;
                                            } else {
                                                lockedCounter = 1;
                                                lockedBy = channel;
                                            }

                                            // reply to the client that operation was successful
                                            buffer.rewind();
                                            buffer.put(LOCK_OK).rewind();
                                            channel.write(buffer);
                                        } else {
                                            locked.add(channel);
                                        }
                                        break;

                                    case UNLOCK_REQUEST: // unlock command
                                        LOGGER.log(Level.INFO, "unlock cmd");
                                        if(channel.equals(lockedBy)) {
                                            // reply to the client that operation was successful
                                            buffer.rewind();
                                            buffer.put(UNLOCK_OK).rewind();
                                            channel.write(buffer);
                                            lockedCounter--;

                                            if(lockedCounter<1 && !locked.isEmpty()) {
                                                // lock is acquired by another client.
                                                lockedBy = locked.remove(0);
                                                lockedCounter = 1;

                                                // reply to the client that operation was successful
                                                ByteBuffer allocate = ByteBuffer.allocate(1);
                                                allocate.rewind();
                                                allocate.put(LOCK_OK).rewind();
                                                lockedBy.write(allocate);
                                            }
                                        } else {
                                            // someone tries to unlock !
                                        }
                                        break;
                                }
                            }
                        }
                        iterator.remove();
                    }
                    selectionKeys.clear();

                }
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Perform a lock request
     *
     * @param socket
     * The socket opened on the lock server
     * @return
     * Returns until the lock is acquired
     * @throws IOException
     */
    public static boolean socketLock(final Socket socket) throws IOException {
        byte[] cmd = new byte[1];

        socket.getOutputStream().write(new byte[]{LOCK_REQUEST});
        int read = socket.getInputStream().read(cmd);

        return read==1 && cmd[0]==LOCK_OK;
    }

    /**
     * Perform an unlock request
     *
     * @param socket
     * The socket opened on the lock server
     * @return
     * Returns once the lock is released
     * @throws IOException
     */
    public static boolean socketUnLock(final Socket socket) throws IOException {
        byte[] cmd = new byte[1];

        socket.getOutputStream().write(new byte[]{UNLOCK_REQUEST});
        int read = socket.getInputStream().read(cmd);

        return read==1 && cmd[0]==UNLOCK_OK;
    }

    public static void socketLockExample() throws IOException, InterruptedException {

        // Create the lock server

        SocketThread socketThread = new SocketThread();
        socketThread.start();

        // Create a set of thread that create a connection to the server used to perform lock and unlock operations.

        List<Thread> threads = new ArrayList<>();
        for(int i=0; i<3; i++) {

            Thread thread = new Thread(Integer.toString(i)) {
                @Override
                public void run() {
                    Socket socket = null;
                    try {
                        socket = new Socket(InetAddress.getLocalHost(), port);
                        Random r = new Random(System.nanoTime());

                        for(int i=0; i<5; i++) {

                            try {
                                System.out.println("Locking in thread " + getName());
                                socketLock(socket);
                                int ms = 100 + r.nextInt(1_000);
                                System.out.println("Locked " + i + " in thread " + getName() + " waiting "  + ms + " ms");
                                try {
                                    Thread.sleep(ms);
                                } catch (InterruptedException ex) {
                                    return;
                                }
                            } finally {
                                socketUnLock(socket);
                                System.out.println("Unlock in thread " + getName());
                            }
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        try {
                            if(socket!=null) {
                                socket.close();
                            }

                        } catch (IOException ex ){
                            throw new RuntimeException(ex);
                        }
                    }
                }
            };
            threads.add(thread);
            thread.start();
        }


        for(Thread thread: threads) {
            thread.join();
        }

        // stop the lock server
        socketThread.interrupt();
        socketThread.join();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        LOGGER.setLevel(Level.SEVERE);
        socketLockExample();
    }
}
