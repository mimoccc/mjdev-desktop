package eu.mjdev.desktop.helpers.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.ImagingOpException;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;

@SuppressWarnings("ALL")
public class ImegeScaler {
    public static final String DEBUG_PROPERTY_NAME = "imgscalr.debug";
    public static final String LOG_PREFIX_PROPERTY_NAME = "imgscalr.logPrefix";
    public static final boolean DEBUG = Boolean.getBoolean(DEBUG_PROPERTY_NAME);
    public static final String LOG_PREFIX = System.getProperty(LOG_PREFIX_PROPERTY_NAME, "[imgscalr] ");
    public static final ConvolveOp OP_ANTIALIAS = new ConvolveOp(
            new Kernel(3, 3, new float[]{.0f, .08f, .0f, .08f, .68f, .08f, .0f, .08f, .0f}
            ),
            ConvolveOp.EDGE_NO_OP,
            null
    );
    public static final RescaleOp OP_DARKER = new RescaleOp(0.9f, 0, null);
    public static final RescaleOp OP_BRIGHTER = new RescaleOp(1.1f, 0, null);
    public static final ColorConvertOp OP_GRAYSCALE = new ColorConvertOp(
            ColorSpace.getInstance(ColorSpace.CS_GRAY),
            null
    );

    static {
        log(0, "Debug output ENABLED");
    }

    public static final int THRESHOLD_BALANCED_SPEED = 1600;
    public static final int THRESHOLD_QUALITY_BALANCED = 800;

    public static BufferedImage apply(
            BufferedImage src,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        long t = -1;
        if (DEBUG)
            t = System.currentTimeMillis();
        if (src == null)
            throw new IllegalArgumentException("src cannot be null");
        if (ops == null || ops.length == 0)
            throw new IllegalArgumentException("ops cannot be null or empty");
        int type = src.getType();
        if (!(type == BufferedImage.TYPE_INT_RGB || type == BufferedImage.TYPE_INT_ARGB))
            src = copyToOptimalImage(src);
        if (DEBUG)
            log(0, "Applying %d BufferedImageOps...", ops.length);
        boolean hasReassignedSrc = false;
        for (int i = 0; i < ops.length; i++) {
            long subT = -1;
            if (DEBUG)
                subT = System.currentTimeMillis();
            BufferedImageOp op = ops[i];
            if (op == null)
                continue;
            if (DEBUG)
                log(1, "Applying BufferedImageOp [class=%s, toString=%s]...",
                        op.getClass(), op.toString());
            Rectangle2D resultBounds = op.getBounds2D(src);
            if (resultBounds == null)
                throw new ImagingOpException(
                        "BufferedImageOp ["
                                + op
                                + "] getBounds2D(src) returned null bounds for the target image; this should not happen and indicates a problem with application of this type of op.");
            BufferedImage dest = createOptimalImage(src,
                    (int) Math.round(resultBounds.getWidth()),
                    (int) Math.round(resultBounds.getHeight()));
            BufferedImage result = op.filter(src, dest);
            if (hasReassignedSrc)
                src.flush();
            src = result;
            hasReassignedSrc = true;
            if (DEBUG)
                log(1,
                        "Applied BufferedImageOp in %d ms, result [width=%d, height=%d]",
                        System.currentTimeMillis() - subT, result.getWidth(),
                        result.getHeight());
        }
        if (DEBUG)
            log(0, "All %d BufferedImageOps applied in %d ms", ops.length,
                    System.currentTimeMillis() - t);
        return src;
    }

    public static BufferedImage crop(
            BufferedImage src,
            int width,
            int height,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        return crop(src, 0, 0, width, height, ops);
    }

    public static BufferedImage crop(
            BufferedImage src,
            int x,
            int y,
            int width,
            int height,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        long t = -1;
        if (DEBUG)
            t = System.currentTimeMillis();
        if (src == null)
            throw new IllegalArgumentException("src cannot be null");
        if (x < 0 || y < 0 || width < 0 || height < 0)
            throw new IllegalArgumentException("Invalid crop bounds: x [" + x
                    + "], y [" + y + "], width [" + width + "] and height ["
                    + height + "] must all be >= 0");
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        if ((x + width) > srcWidth)
            throw new IllegalArgumentException(
                    "Invalid crop bounds: x + width [" + (x + width)
                            + "] must be <= src.getWidth() [" + srcWidth + "]");
        if ((y + height) > srcHeight)
            throw new IllegalArgumentException(
                    "Invalid crop bounds: y + height [" + (y + height)
                            + "] must be <= src.getHeight() [" + srcHeight
                            + "]");
        if (DEBUG)
            log(0,
                    "Cropping Image [width=%d, height=%d] to [x=%d, y=%d, width=%d, height=%d]...",
                    srcWidth, srcHeight, x, y, width, height);
        BufferedImage result = createOptimalImage(src, width, height);
        Graphics g = result.getGraphics();
        g.drawImage(src, 0, 0, width, height, x, y, (x + width), (y + height), null);
        g.dispose();
        if (DEBUG)
            log(0, "Cropped Image in %d ms", System.currentTimeMillis() - t);
        if (ops != null && ops.length > 0)
            result = apply(result, ops);
        return result;
    }

    public static BufferedImage pad(
            BufferedImage src,
            int padding,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        return pad(src, padding, Color.BLACK, ops);
    }

    public static BufferedImage pad(
            BufferedImage src,
            int padding,
            Color color,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        long t = -1;
        if (DEBUG)
            t = System.currentTimeMillis();
        if (src == null)
            throw new IllegalArgumentException("src cannot be null");
        if (padding < 1)
            throw new IllegalArgumentException("padding [" + padding + "] must be > 0");
        if (color == null)
            throw new IllegalArgumentException("color cannot be null");
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int sizeDiff = (padding * 2);
        int newWidth = srcWidth + sizeDiff;
        int newHeight = srcHeight + sizeDiff;
        if (DEBUG)
            log(0,
                    "Padding Image from [originalWidth=%d, originalHeight=%d, padding=%d] to [newWidth=%d, newHeight=%d]...",
                    srcWidth, srcHeight, padding, newWidth, newHeight);

        boolean colorHasAlpha = (color.getAlpha() != 255);
        boolean imageHasAlpha = (src.getTransparency() != BufferedImage.OPAQUE);
        BufferedImage result;
        if (colorHasAlpha || imageHasAlpha) {
            if (DEBUG)
                log(1,
                        "Transparency FOUND in source image or color, using ARGB image type...");
            result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        } else {
            if (DEBUG)
                log(1,
                        "Transparency NOT FOUND in source image or color, using RGB image type...");
            result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        }
        Graphics g = result.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, newWidth, padding);
        g.fillRect(0, padding, padding, newHeight);
        g.fillRect(padding, newHeight - padding, newWidth, newHeight);
        g.fillRect(newWidth - padding, padding, newWidth, newHeight - padding);
        g.drawImage(src, padding, padding, null);
        g.dispose();
        if (DEBUG)
            log(0, "Padding Applied in %d ms", System.currentTimeMillis() - t);
        if (ops != null && ops.length > 0)
            result = apply(result, ops);
        return result;
    }

    public static BufferedImage resize(
            BufferedImage src,
            int targetSize,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        return resize(src, Method.AUTOMATIC, Mode.AUTOMATIC, targetSize, targetSize, ops);
    }

    public static BufferedImage resize(
            BufferedImage src,
            Method scalingMethod,
            int targetSize,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        return resize(src, scalingMethod, Mode.AUTOMATIC, targetSize, targetSize, ops);
    }

    public static BufferedImage resize(
            BufferedImage src,
            Mode resizeMode,
            int targetSize,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        return resize(src, Method.AUTOMATIC, resizeMode, targetSize, targetSize, ops);
    }

    public static BufferedImage resize(
            BufferedImage src,
            Method scalingMethod,
            Mode resizeMode,
            int targetSize,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        return resize(src, scalingMethod, resizeMode, targetSize, targetSize, ops);
    }

    public static BufferedImage resize(
            BufferedImage src,
            int targetWidth,
            int targetHeight,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        return resize(src, Method.AUTOMATIC, Mode.AUTOMATIC, targetWidth, targetHeight, ops);
    }

    public static BufferedImage resize(
            BufferedImage src,
            Method scalingMethod,
            int targetWidth,
            int targetHeight,
            BufferedImageOp... ops
    ) {
        return resize(src, scalingMethod, Mode.AUTOMATIC, targetWidth, targetHeight, ops);
    }

    public static BufferedImage resize(
            BufferedImage src,
            Mode resizeMode,
            int targetWidth,
            int targetHeight,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        return resize(src, Method.AUTOMATIC, resizeMode, targetWidth, targetHeight, ops);
    }

    public static BufferedImage resize(
            BufferedImage src,
            Method scalingMethod,
            Mode resizeMode,
            int targetWidth,
            int targetHeight,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        long t = -1;
        if (DEBUG)
            t = System.currentTimeMillis();
        if (src == null)
            throw new IllegalArgumentException("src cannot be null");
        if (targetWidth < 0)
            throw new IllegalArgumentException("targetWidth must be >= 0");
        if (targetHeight < 0)
            throw new IllegalArgumentException("targetHeight must be >= 0");
        if (scalingMethod == null)
            throw new IllegalArgumentException(
                    "scalingMethod cannot be null. A good default value is Method.AUTOMATIC.");
        if (resizeMode == null)
            throw new IllegalArgumentException(
                    "resizeMode cannot be null. A good default value is Mode.AUTOMATIC.");
        BufferedImage result = null;
        int currentWidth = src.getWidth();
        int currentHeight = src.getHeight();
        float ratio = ((float) currentHeight / (float) currentWidth);
        if (DEBUG)
            log(0,
                    "Resizing Image [size=%dx%d, resizeMode=%s, orientation=%s, ratio(H/W)=%f] to [targetSize=%dx%d]",
                    currentWidth, currentHeight, resizeMode,
                    (ratio <= 1 ? "Landscape/Square" : "Portrait"), ratio,
                    targetWidth, targetHeight);
        if (resizeMode == Mode.FIT_EXACT) {
            if (DEBUG)
                log(1,
                        "Resize Mode FIT_EXACT used, no width/height checking or re-calculation will be done.");
        } else if (resizeMode == Mode.BEST_FIT_BOTH) {
            float requestedHeightScaling = ((float) targetHeight / (float) currentHeight);
            float requestedWidthScaling = ((float) targetWidth / (float) currentWidth);
            float actualScaling = Math.min(requestedHeightScaling, requestedWidthScaling);
            targetHeight = Math.round((float) currentHeight * actualScaling);
            targetWidth = Math.round((float) currentWidth * actualScaling);
            if (targetHeight == currentHeight && targetWidth == currentWidth)
                return src;
            if (DEBUG)
                log(1, "Auto-Corrected width and height based on scalingRatio %d.", actualScaling);
        } else {
            if ((ratio <= 1 && resizeMode == Mode.AUTOMATIC)
                    || (resizeMode == Mode.FIT_TO_WIDTH)) {
                if (targetWidth == src.getWidth()) {
                    return src;
                }
                int originalTargetHeight = targetHeight;
                targetHeight = (int) Math.ceil((float) targetWidth * ratio);
                if (DEBUG && originalTargetHeight != targetHeight)
                    log(1,
                            "Auto-Corrected targetHeight [from=%d to=%d] to honor image proportions.",
                            originalTargetHeight, targetHeight);
            } else {
                if (targetHeight == src.getHeight())
                    return src;
                int originalTargetWidth = targetWidth;
                targetWidth = Math.round((float) targetHeight / ratio);
                if (DEBUG && originalTargetWidth != targetWidth)
                    log(1,
                            "Auto-Corrected targetWidth [from=%d to=%d] to honor image proportions.",
                            originalTargetWidth, targetWidth);
            }
        }
        if (scalingMethod == Method.AUTOMATIC)
            scalingMethod = determineScalingMethod(targetWidth, targetHeight, ratio);
        if (DEBUG)
            log(1, "Using Scaling Method: %s", scalingMethod);
        if (scalingMethod == Method.SPEED) {
            result = scaleImage(src, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        } else if (scalingMethod == Method.BALANCED) {
            result = scaleImage(src, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        } else if (scalingMethod == Method.QUALITY || scalingMethod == Method.ULTRA_QUALITY) {
            if (targetWidth > currentWidth || targetHeight > currentHeight) {
                if (DEBUG)
                    log(1,
                            "QUALITY scale-up, a single BICUBIC scale operation will be used...");
                result = scaleImage(src, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            } else {
                if (DEBUG)
                    log(1,
                            "QUALITY scale-down, incremental scaling will be used...");
                result = scaleImageIncrementally(
                        src,
                        targetWidth,
                        targetHeight,
                        scalingMethod,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC
                );
            }
        }
        if (DEBUG) {
            log(0, "Resized Image in %d ms", System.currentTimeMillis() - t);
        }
        if (ops != null && ops.length > 0) {
            result = apply(result, ops);
        }
        return result;
    }

    public static BufferedImage rotate(
            BufferedImage src, Rotation rotation,
            BufferedImageOp... ops
    ) throws IllegalArgumentException, ImagingOpException {
        long t = -1;
        if (DEBUG)
            t = System.currentTimeMillis();
        if (src == null)
            throw new IllegalArgumentException("src cannot be null");
        if (rotation == null)
            throw new IllegalArgumentException("rotation cannot be null");
        if (DEBUG)
            log(0, "Rotating Image [%s]...", rotation);
        int newWidth = src.getWidth();
        int newHeight = src.getHeight();
        AffineTransform tx = new AffineTransform();
        switch (rotation) {
            case CW_90:
                newWidth = src.getHeight();
                newHeight = src.getWidth();
                tx.translate(newWidth, 0);
                tx.quadrantRotate(1);
                break;
            case CW_270:
                newWidth = src.getHeight();
                newHeight = src.getWidth();
                tx.translate(0, newHeight);
                tx.quadrantRotate(3);
                break;
            case CW_180:
                tx.translate(newWidth, newHeight);
                tx.quadrantRotate(2);
                break;
            case FLIP_HORZ:
                tx.translate(newWidth, 0);
                tx.scale(-1.0, 1.0);
                break;
            case FLIP_VERT:
                tx.translate(0, newHeight);
                tx.scale(1.0, -1.0);
                break;
        }
        BufferedImage result = createOptimalImage(src, newWidth, newHeight);
        Graphics2D g2d = (Graphics2D) result.createGraphics();
        g2d.drawImage(src, tx, null);
        g2d.dispose();
        if (DEBUG)
            log(0, "Rotation Applied in %d ms, result [width=%d, height=%d]",
                    System.currentTimeMillis() - t, result.getWidth(),
                    result.getHeight());
        if (ops != null && ops.length > 0)
            result = apply(result, ops);
        return result;
    }

    protected static void log(int depth, String message, Object... params) {
        if (ImegeScaler.DEBUG) {
            System.out.print(ImegeScaler.LOG_PREFIX);
            for (int i = 0; i < depth; i++)
                System.out.print("\t");
            System.out.printf(message, params);
            System.out.println();
        }
    }

    protected static BufferedImage createOptimalImage(BufferedImage src) {
        return createOptimalImage(src, src.getWidth(), src.getHeight());
    }

    protected static BufferedImage createOptimalImage(
            BufferedImage src,
            int width,
            int height
    ) throws IllegalArgumentException {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("width [" + width
                    + "] and height [" + height + "] must be > 0");
        return new BufferedImage(
                width,
                height,
                (src.getTransparency() == Transparency.OPAQUE
                        ? BufferedImage.TYPE_INT_RGB
                        : BufferedImage.TYPE_INT_ARGB)
        );
    }

    protected static BufferedImage copyToOptimalImage(
            BufferedImage src
    ) throws IllegalArgumentException {
        if (src == null) {
            throw new IllegalArgumentException("src cannot be null");
        }
        int type = (src.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), type);
        Graphics g = result.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return result;
    }

    protected static Method determineScalingMethod(
            int targetWidth,
            int targetHeight,
            float ratio
    ) {
        int length = (ratio <= 1 ? targetWidth : targetHeight);
        Method result = Method.SPEED;
        if (length <= ImegeScaler.THRESHOLD_QUALITY_BALANCED)
            result = Method.QUALITY;
        else if (length <= ImegeScaler.THRESHOLD_BALANCED_SPEED)
            result = Method.BALANCED;
        if (DEBUG)
            log(2, "AUTOMATIC scaling method selected: %s", result.name());
        return result;
    }

    protected static BufferedImage scaleImage(
            BufferedImage src,
            int targetWidth,
            int targetHeight,
            Object interpolationHintValue
    ) {
        BufferedImage result = createOptimalImage(src, targetWidth, targetHeight);
        Graphics2D resultGraphics = result.createGraphics();
        resultGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHintValue);
        resultGraphics.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        resultGraphics.dispose();
        return result;
    }

    protected static BufferedImage scaleImageIncrementally(
            BufferedImage src,
            int targetWidth,
            int targetHeight,
            Method scalingMethod,
            Object interpolationHintValue
    ) {
        boolean hasReassignedSrc = false;
        int incrementCount = 0;
        int currentWidth = src.getWidth();
        int currentHeight = src.getHeight();
        int fraction = (scalingMethod == Method.ULTRA_QUALITY ? 7 : 2);
        do {
            int prevCurrentWidth = currentWidth;
            int prevCurrentHeight = currentHeight;
            if (currentWidth > targetWidth) {
                currentWidth -= (currentWidth / fraction);
                if (currentWidth < targetWidth)
                    currentWidth = targetWidth;
            }
            if (currentHeight > targetHeight) {
                currentHeight -= (currentHeight / fraction);
                if (currentHeight < targetHeight)
                    currentHeight = targetHeight;
            }
            if (prevCurrentWidth == currentWidth && prevCurrentHeight == currentHeight)
                break;
            if (DEBUG)
                log(2, "Scaling from [%d x %d] to [%d x %d]", prevCurrentWidth,
                        prevCurrentHeight, currentWidth, currentHeight);
            BufferedImage incrementalImage = scaleImage(src, currentWidth, currentHeight, interpolationHintValue);
            if (hasReassignedSrc) {
                src.flush();
            }
            src = incrementalImage;
            hasReassignedSrc = true;
            incrementCount++;
        } while (currentWidth != targetWidth || currentHeight != targetHeight);
        if (DEBUG) {
            log(2, "Incrementally Scaled Image in %d steps.", incrementCount);
        }
        return src;
    }
}
