/*
* This file is based on or incorporates material from the projects listed below (Third Party IP). 
* The original copyright notice and the license under which Microsoft received such Third Party IP, 
* are set forth below. Such licenses and notices are provided for informational purposes only. 
* Microsoft licenses the Third Party IP to you under the licensing terms for the Microsoft product. 
* Microsoft reserves all other rights not expressly granted under this agreement, whether by implication, 
* estoppel or otherwise.
* 
* TensorFlow (Android example)
* Copyright 2017 The TensorFlow Authors.  All rights reserved.
* Provided for Informational Purposes Only
* Apache 2.0 License
* Licensed under the Apache License, Version 2.0 (the License); you may not use this file except in compliance 
* with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed 
* on an "AS-IS" BASES, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.     
* See the License for specific language governing permissions and limitations under the License.
*/

package demo.tensorflow.org.customvision_sample;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import junit.framework.Assert;

import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

public class MSCognitiveServicesClassifier {

    private static final String MODEL_FILE = "file:///android_asset/model.pb";

    private TensorFlowInferenceInterface inferenceInterface;
    private Vector<String> labels = new Vector<String>();
    private int numberOfClasses = 0;
    private boolean hasNormalizationLayer = false;

    private static final int INPUT_SIZE = 227;
    private static final int RESIZE_SIZE = 256;
    private static final String INPUT_NAME = "Placeholder";
    private static final String OUTPUT_NAME = "loss";
    private static final String[] DATA_NORM_LAYER_PREFIX = {"data_bn", "BatchNorm1"};

    static {
        System.loadLibrary("tensorflow_inference");
    }

    public MSCognitiveServicesClassifier(final Context context) {
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);

        // Look to see if this graph has a data normalization layer, if so we don't need to do
        // mean subtraction on the image.
        java.util.Iterator<org.tensorflow.Operation> opIter = inferenceInterface.graph().operations();
        while (opIter.hasNext() && !hasNormalizationLayer) {
            org.tensorflow.Operation op = opIter.next();
            for (String normLayerPrefix : DATA_NORM_LAYER_PREFIX) {
                if (op.name().contains(normLayerPrefix)) {
                    hasNormalizationLayer = true;
                    break;
                }
            }
        }

        loadLabels(context);
    }

    private void loadLabels(final Context context) {
        final AssetManager assetManager = context.getAssets();

        // loading labels
        BufferedReader br = null;
        try {
            final InputStream inputStream = assetManager.open("labels.txt");
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
            }
            br.close();

            numberOfClasses = labels.size();
        } catch (IOException e) {
            throw new RuntimeException("error reading labels file!", e);
        }
    }

    public Classifier.Recognition classifyImage(Bitmap sourceImage, int orientation) {

        Bitmap resizedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);

        cropAndRescaleBitmap(sourceImage, resizedBitmap, orientation);

        String[] outputNames = new String[]{OUTPUT_NAME};
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        float[] floatValues = new float[INPUT_SIZE * INPUT_SIZE * 3];
        float[] outputs = new float[numberOfClasses];

        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());

        final float IMAGE_MEAN_R;
        final float IMAGE_MEAN_G;
        final float IMAGE_MEAN_B;
        if (hasNormalizationLayer)
        {
            // Mean subtraction is baked into the model.
            IMAGE_MEAN_R = 0.f;
            IMAGE_MEAN_G = 0.f;
            IMAGE_MEAN_B = 0.f;
        }
        else
        {
            // This is an older model without mean normalization layer and needs to do mean subtraction.
            IMAGE_MEAN_R = 124.f;
            IMAGE_MEAN_G = 117.f;
            IMAGE_MEAN_B = 105.f;
        }

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (float)(val & 0xFF) - IMAGE_MEAN_B;
            floatValues[i * 3 + 1] = (float)((val >> 8) & 0xFF) - IMAGE_MEAN_G;
            floatValues[i * 3 + 2] = (float)((val >> 16) & 0xFF) - IMAGE_MEAN_R;
        }

        inferenceInterface.feed(INPUT_NAME, floatValues, 1, INPUT_SIZE, INPUT_SIZE, 3);
        inferenceInterface.run(outputNames);
        inferenceInterface.fetch(OUTPUT_NAME, outputs);

        int maxIndex = -1;
        float maxConf = 0.f;

        for (int i = 0; i < outputs.length; ++i) {
            if (outputs[i] > maxConf) {
                maxConf = outputs[i];
                maxIndex = i;
            }
        }

        return new Classifier.Recognition("0", labels.get(maxIndex), maxConf, null);
    }

    // function copied from TensorFlow samples
    // Copyright 2017 The TensorFlow Authors.  All rights reserved.
    public static void cropAndRescaleBitmap(final Bitmap src, final Bitmap dst, int sensorOrientation) {
        Assert.assertEquals(dst.getWidth(), dst.getHeight());
        final float maxDim = Math.max(src.getWidth(), src.getHeight());

        final Matrix matrix = new Matrix();

        // Scale to max dim of 1600 first
        if (maxDim > 1600) {
            final float scale = (src.getWidth() > src.getHeight()) ?
                    1600.0f / src.getWidth() :
                    1600.0f / src.getHeight();
            matrix.preScale(scale, scale);
        }

        final float minDim = Math.min(src.getWidth(), src.getHeight());
        // We only want the center square out of the original rectangle.
        final float translateX = -Math.max(0, (src.getWidth() - minDim) / 2);
        final float translateY = -Math.max(0, (src.getHeight() - minDim) / 2);
        matrix.preTranslate(translateX, translateY);

        // Resize down to RESIZE_SIZE
        final float scaleFactor = RESIZE_SIZE / minDim;
        matrix.postScale(scaleFactor, scaleFactor);

        // Rotate around the center if necessary.
        if (sensorOrientation != 0) {
            matrix.postTranslate(-dst.getWidth() / 2.0f, -dst.getHeight() / 2.0f);
            matrix.postRotate(sensorOrientation);
            matrix.postTranslate(dst.getWidth() / 2.0f, dst.getHeight() / 2.0f);
        }

        // Center crop the out an INPUT_SIZE rectangle
        matrix.postTranslate(-(RESIZE_SIZE - INPUT_SIZE) / 2, -(RESIZE_SIZE - INPUT_SIZE) / 2);

        final Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, matrix, null);
    }
}
