package eu.mjdev.desktop.helpers.image.filters;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

@SuppressWarnings("ALL")
public abstract class AbstractBufferedImageOp implements BufferedImageOp {
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null)
            dstCM = src.getColorModel();
        return new BufferedImage(
                dstCM,
                dstCM.createCompatibleWritableRaster(
                        src.getWidth(),
                        src.getHeight()),
                dstCM.isAlphaPremultiplied(),
                null
        );
    }

    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null)
            dstPt = new Point2D.Double();
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    public RenderingHints getRenderingHints() {
        return null;
    }

    public int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
            return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);
        return image.getRGB(x, y, width, height, pixels, 0, width);
    }

    public void setRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
            image.getRaster().setDataElements(x, y, width, height, pixels);
        else
            image.setRGB(x, y, width, height, pixels, 0, width);
    }
}
