package com.sprd.validationtools.camera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.graphics.ImageFormat;
import android.util.Log;

public class ImageUtils {

    private static final String TAG = "ImageUtils";
    
    public static void writeYuvImage(Image img, OutputStream out) throws IOException {
        if (img.getFormat() != ImageFormat.YUV_420_888) {
            Log.w(TAG,String.format(
                    "Unexpected Image format: %d, expected ImageFormat.YUV_420_888",
                    img.getFormat()));
        }
        WritableByteChannel outChannel = Channels.newChannel(out);
        for (int plane = 0; plane < 3; plane++) {
            Image.Plane colorPlane = img.getPlanes()[plane];
            ByteBuffer colorData = colorPlane.getBuffer();
            int subsampleFactor = (plane == 0) ? 1 : 2;
            int colorW = img.getWidth() / subsampleFactor;
            int colorH = img.getHeight() / subsampleFactor;
            colorData.rewind();
            colorData.limit(colorData.capacity());
            if (colorPlane.getPixelStride() == 1) {
                // Can write contiguous rows
                for (int y = 0, rowStart = 0; y < colorH; y++, rowStart += colorPlane
                        .getRowStride()) {
                    colorData.limit(rowStart + colorW);
                    colorData.position(rowStart);
                    outChannel.write(colorData);
                }
            } else {
                // Need to pack rows
                byte[] row = new byte[(colorW - 1)
                        * colorPlane.getPixelStride() + 1];
                byte[] packedRow = new byte[colorW];
                ByteBuffer packedRowBuffer = ByteBuffer.wrap(packedRow);
                for (int y = 0, rowStart = 0; y < colorH; y++, rowStart += colorPlane
                        .getRowStride()) {
                    colorData.position(rowStart);
                    colorData.get(row);
                    for (int x = 0, i = 0; x < colorW; x++, i += colorPlane
                            .getPixelStride()) {
                        packedRow[x] = row[i];
                    }
                    packedRowBuffer.rewind();
                    outChannel.write(packedRowBuffer);
                }
            }
        }
    }
}
