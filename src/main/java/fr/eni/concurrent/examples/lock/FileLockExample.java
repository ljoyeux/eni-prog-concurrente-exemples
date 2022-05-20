package fr.eni.concurrent.examples.lock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ljoyeux on 24/06/2017.
 */
public class FileLockExample {

    public static String getJava() {
        final String javaHome = System.getProperty("java.home");
        final String pathSeparator = System.getProperty("file.separator");
        final String java = javaHome + pathSeparator + "bin" + pathSeparator + "java";

        return java;
    }

    public static String getClasspath() throws URISyntaxException {
        final URL[] urls = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
        final StringBuilder sb = new StringBuilder();

        boolean firstUrl = true;
        for(URL url: urls) {
            if(!firstUrl) {
                sb.append(":");
            }

            sb.append(new File(url.toURI()).getAbsoluteFile());
            firstUrl = false;
        }

        String classPath = sb.toString();

        return classPath;
    }


    public static void lockFile(final String lockFileName) throws IOException, URISyntaxException {
        final String processName = (lockFileName==null) ? "Parent" : "Child";
        System.out.println("In " + processName + ((lockFileName!=null) ? " " + lockFileName : ""));

        final File file;
        if(lockFileName==null) {
            file = File.createTempFile("lock-", ".lock");
            file.deleteOnExit();

        } else {
            file = new File(lockFileName);
        }

        final FileOutputStream fos = new FileOutputStream(file);
        final FileChannel channel = fos.getChannel();
        System.out.println(processName + " try to lock");
        final FileLock lock = channel.lock();
        System.out.println(processName + " locked");


        if(lockFileName==null) {
            final List<String> args = Arrays.asList(getJava(), "-classpath", getClasspath(), FileLockExample.class.getName(), file.getAbsolutePath());
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

            try {
                System.out.println("Parent wait 5s");
                Thread.sleep(5_000);
            } catch (InterruptedException ex) {
            }
            lock.release();
            System.out.println(processName + " release");

            try {
                Thread.sleep(5_000);
            } catch (InterruptedException ex) {
            }

            thread.interrupt();

        } else {
            lock.release();
            System.out.println(processName + " release");
        }
    }

    public static void main(final String[] args) throws IOException, URISyntaxException {
        lockFile(args.length>0 ? args[0] : null);
    }
}
