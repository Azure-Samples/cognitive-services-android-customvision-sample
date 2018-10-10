/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package demo.tensorflow.org.customvision_sample.env;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Utility class for manipulating images.
 **/
public class ImageUtils {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = new Logger();

  // This value is 2 ^ 18 - 1, and is used to clamp the RGB values before their ranges
  // are normalized to eight bits.
  static final int kMaxChannelValue = 262143;

  public static void convertYUV420ToARGB8888(
      byte[] yData,
      byte[] uData,
      byte[] vData,
      int width,
      int height,
      int yRowStride,
      int uvRowStride,
      int uvPixelStride,
      int[] out) {

    int i = 0;
    for (int y = 0; y < height; y++) {
      int pY = yRowStride * y;
      int uv_row_start = uvRowStride * (y >> 1);
      int pUV = uv_row_start;

      for (int x = 0; x < width; x++) {
        int uv_offset = pUV + (x >> 1) * uvPixelStride;
        out[i++] =
            YUV2RGB(
                convertByteToInt(yData, pY + x),
                convertByteToInt(uData, uv_offset),
                convertByteToInt(vData, uv_offset));
      }
    }
  }

  /**
   * Converts YUV420 semi-planar data to ARGB 8888 data using the supplied width and height. The
   * input and output must already be allocated and non-null. For efficiency, no error checking is
   * performed.
   *
   * @param input The array of YUV 4:2:0 input data.
   * @param output A pre-allocated array for the ARGB 8:8:8:8 output data.
   * @param width The width of the input image.
   * @param height The height of the input image.
   */
  public static void convertYUV420SPToARGB8888(
          byte[] input, int[] output, int width, int height)
  {
    int pY = 0;
    for (int i=0; i<height; i++) {
      int pUV = width * height + (i / 2) * width;
      int u = 0;
      int v = 0;
      for (int j=0; j<width; j++) {
        int y = convertByteToInt(input, pY);
        if (j % 2 == 0)
        {
          v = convertByteToInt(input, pUV++);
          u = convertByteToInt(input, pUV++);
        }
        output[pY++] = YUV2RGB(y, u, v);
      }
    }
  }

  private static int convertByteToInt(byte[] arr, int pos) {
    return arr[pos] & 0xFF;
  }

  private static int YUV2RGB(int nY, int nU, int nV) {
    nY -= 16;
    nU -= 128;
    nV -= 128;
    if (nY < 0) nY = 0;

    // This is the floating point equivalent. We do the conversion in integer
    // because some Android devices do not have floating point in hardware.
    // nR = (int)(1.164 * nY + 2.018 * nU);
    // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
    // nB = (int)(1.164 * nY + 1.596 * nV);

    final int foo = 1192 * nY;
    int nR = foo + 1634 * nV;
    int nG = foo - 833 * nV - 400 * nU;
    int nB = foo + 2066 * nU;

    nR = Math.min(kMaxChannelValue, Math.max(0, nR));
    nG = Math.min(kMaxChannelValue, Math.max(0, nG));
    nB = Math.min(kMaxChannelValue, Math.max(0, nB));

    return 0xff000000 | ((nR << 6) & 0x00ff0000) | ((nG >> 2) & 0x0000FF00) | ((nB >> 10) & 0xff);
  }

}
