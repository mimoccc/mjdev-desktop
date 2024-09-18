package eu.mjdev.desktop.helpers.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImagingOpException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ALL")
public class AsyncImageScaler {
    public static final String THREAD_COUNT_PROPERTY_NAME = "imgscalr.async.threadCount";
    public static final int THREAD_COUNT = Integer.getInteger(THREAD_COUNT_PROPERTY_NAME, 2);

    static {
        if (THREAD_COUNT < 1)
            throw new RuntimeException("System property '"
                    + THREAD_COUNT_PROPERTY_NAME + "' set THREAD_COUNT to "
                    + THREAD_COUNT + ", but THREAD_COUNT must be > 0.");
    }

    protected static ExecutorService service;

    public static Future<BufferedImage> apply(
            final BufferedImage src,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(() -> ImegeScaler.apply(src, ops));
    }

    public static Future<BufferedImage> crop(
            final BufferedImage src,
            final int width, final int height,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(new Callable<BufferedImage>() {
            public BufferedImage call() throws Exception {
                return ImegeScaler.crop(src, width, height, ops);
            }
        });
    }

    public static Future<BufferedImage> crop(
            final BufferedImage src,
            final int x,
            final int y,
            final int width,
            final int height,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(new Callable<BufferedImage>() {
            public BufferedImage call() throws Exception {
                return ImegeScaler.crop(src, x, y, width, height, ops);
            }
        });
    }

    public static Future<BufferedImage> pad(
            final BufferedImage src,
            final int padding,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(() -> ImegeScaler.pad(src, padding, ops));
    }

    public static Future<BufferedImage> pad(
            final BufferedImage src,
            final int padding,
            final Color color,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(() -> ImegeScaler.pad(src, padding, color, ops));
    }

    public static Future<BufferedImage> resize(
            final BufferedImage src,
            final int targetSize, final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(() -> ImegeScaler.resize(src, targetSize, ops));
    }

    public static Future<BufferedImage> resize(
            final BufferedImage src,
            final Method scalingMethod,
            final int targetSize,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(() -> ImegeScaler.resize(src, scalingMethod, targetSize, ops));
    }

    public static Future<BufferedImage> resize(
            final BufferedImage src,
            final Mode resizeMode,
            final int targetSize,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(new Callable<BufferedImage>() {
            public BufferedImage call() throws Exception {
                return ImegeScaler.resize(src, resizeMode, targetSize, ops);
            }
        });
    }

    public static Future<BufferedImage> resize(
            final BufferedImage src,
            final Method scalingMethod,
            final Mode resizeMode,
            final int targetSize,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(() -> ImegeScaler.resize(src, scalingMethod, resizeMode, targetSize, ops));
    }

    public static Future<BufferedImage> resize(
            final BufferedImage src,
            final int targetWidth,
            final int targetHeight,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(() -> ImegeScaler.resize(src, targetWidth, targetHeight, ops));
    }

    public static Future<BufferedImage> resize(
            final BufferedImage src,
            final Method scalingMethod,
            final int targetWidth,
            final int targetHeight,
            final BufferedImageOp... ops
    ) {
        checkService();
        return service.submit(() -> ImegeScaler.resize(src, scalingMethod, targetWidth, targetHeight, ops));
    }

    public static Future<BufferedImage> resize(
            final BufferedImage src,
            final Mode resizeMode,
            final int targetWidth,
            final int targetHeight,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(() -> ImegeScaler.resize(src, resizeMode, targetWidth, targetHeight, ops));
    }

    public static Future<BufferedImage> resize(
            final BufferedImage src,
            final Method scalingMethod,
            final Mode resizeMode,
            final int targetWidth,
            final int targetHeight,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(() -> ImegeScaler.resize(src, scalingMethod, resizeMode, targetWidth, targetHeight, ops));
    }

    public static Future<BufferedImage> rotate(
            final BufferedImage src,
            final Rotation rotation,
            final BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        checkService();
        return service.submit(() -> ImegeScaler.rotate(src, rotation, ops));
    }

    protected static ExecutorService createService() {
        return createService(new DefaultThreadFactory());
    }

    protected static ExecutorService createService(
            ThreadFactory factory
    ) throws IllegalArgumentException {
        if (factory == null) {
            throw new IllegalArgumentException("factory cannot be null");
        }
        return Executors.newFixedThreadPool(THREAD_COUNT, factory);
    }

    protected static void checkService() {
        if (service == null || service.isShutdown() || service.isTerminated()) {
            service = createService();
        }
    }

    protected static class DefaultThreadFactory implements ThreadFactory {
        protected static final AtomicInteger poolNumber = new AtomicInteger(1);

        protected final ThreadGroup group;
        protected final AtomicInteger threadNumber = new AtomicInteger(1);
        protected final String namePrefix;

        @SuppressWarnings("removal")
        DefaultThreadFactory() {
            SecurityManager manager = System.getSecurityManager();
            group = (manager == null ? Thread.currentThread().getThreadGroup() : manager.getThreadGroup());
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

    protected static class ServerThreadFactory extends DefaultThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = super.newThread(r);
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        }
    }
}
