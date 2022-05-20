package fr.eni.concurrent.examples.io;

import fr.eni.concurrent.examples.lock.FileLockExample;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ljoyeux on 05/07/2017.
 */
public class MappedFileExample {

    public static final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;

    public static void mapExample(String filename) throws URISyntaxException, IOException {
        final String processName = (filename==null) ? "Parent" : "Child";
        System.out.println("In " + processName + ((filename!=null) ? " " + filename : ""));

        final File file;
        if(filename==null) {
            file = File.createTempFile("lock-", ".lock");
            file.deleteOnExit();

        } else {
            file = new File(filename);
        }

        if(filename==null) {
            // in Parent

            // create the file
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final FileChannel channel = randomAccessFile.getChannel();
            final int fileSize = 1024*1024;
            final MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            FileLock firstLock = channel.lock(0, INTEGER_BYTES, false);
            System.out.println("Parent : create file " + file.getAbsolutePath());




            // start another process which will access the same file

            final List<String> args = Arrays.asList(FileLockExample.getJava(), "-classpath", FileLockExample.getClasspath(), MappedFileExample.class.getName(), file.getAbsolutePath());
            final ProcessBuilder pb = new ProcessBuilder(args);
            final Process process  = pb.start();
            final InputStream inputStream = process.getInputStream();
            final Thread thread = new Thread() {
                @Override
                public void run() {
                    final byte[] chunk = new byte[8192];
                    try {
                        for (; !isInterrupted(); ) {
                            int read = inputStream.read(chunk);
                            if (read < 0) {
                                break;
                            }

                            if (read > 0) {
                                System.out.print(new String(chunk, 0, read));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();
            System.out.println("Parent Start child process");

            // wait for child process
            try {
                System.out.println("Parent wait 2s");
                Thread.sleep(2_000);
            } catch (InterruptedException ex) {
            }

            // Write some data
            System.out.println("Parent write data");
            final IntBuffer intBuffer = map.asIntBuffer();
            for(int i=0; i<fileSize / INTEGER_BYTES; i++) {
                intBuffer.put(i);
            }

            map.force(); // commit changes

            firstLock.release();
            System.out.println("Parent release");

            FileLock secondLock = channel.lock(INTEGER_BYTES, INTEGER_BYTES, false);
            intBuffer.rewind();
            int[] chunk = new int[10];
            System.out.println("Parent Read values");
            intBuffer.get(chunk);
            for(int i=0; i<10; i++) {
                System.out.print(chunk[i] + ", ");
                chunk[i]++;
            }
            secondLock.release();


            randomAccessFile.close();
            thread.interrupt();

        } else {
            // in child
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final FileChannel channel = randomAccessFile.getChannel();
            final int fileSize = 1024*1024;
            System.out.println("Child locking file");
            FileLock secondLock = channel.lock(INTEGER_BYTES, INTEGER_BYTES, false);
            FileLock firstLock = channel.lock(0, INTEGER_BYTES, false);
            System.out.println("Child file locked");

            final MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            IntBuffer intBuffer = map.asIntBuffer();
            int[] chunk = new int[10];
            System.out.println("Child Read values");
            intBuffer.get(chunk);
            for(int i=0; i<10; i++) {
                System.out.print(chunk[i] + ", ");
                chunk[i]++;
            }
            System.out.println();
            intBuffer.rewind();
            intBuffer.put(chunk);
            map.force();
            firstLock.release();
            secondLock.release();
            System.out.println(" Child release");

            randomAccessFile.close();
        }
    }

    public static void trivialFileMapExample() throws IOException {
        final File file = File.createTempFile("lock-", ".lock");

        System.out.println(file.getAbsolutePath());

        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        final int fileSize = 1024*1024;
        final MappedByteBuffer map = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
        try {
            final byte[] array = map.array();
        } catch (UnsupportedOperationException ex ) {
            ex.printStackTrace(); // operation is not supported
        }

        // Write some data
        final IntBuffer intBuffer = map.asIntBuffer();
        for(int i=0; i<fileSize / INTEGER_BYTES; i++) {
            intBuffer.put(i);
        }

        map.force(); // commit changes

        randomAccessFile.close();
//        file.deleteOnExit();

    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        mapExample(args.length>0 ? args[0] : null);
    }
}
