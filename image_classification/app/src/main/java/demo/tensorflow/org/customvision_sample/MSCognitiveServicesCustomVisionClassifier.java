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
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import ai.customvision.CustomVisionManager;
import ai.customvision.tflite.ImageClassifier;

public class MSCognitiveServicesCustomVisionClassifier implements ICognitiveServicesClassifier, AutoCloseable {
    private static String TAG = MSCognitiveServicesCustomVisionClassifier.class.getSimpleName();

    // Specify the path of manifest file about the model to use
    private static String ModelManifestPath = "sample-tflite.cvmodel/cvexport.manifest";  // TensorFlow Lite model (.tflite)

    /**
     * ImageClassifier instance responsible to run inference
     */
    private ImageClassifier classifierRuntime;

    /**
     * Initialize an instance of this wrapper class to run inference.
     * @param context Application context to load model file from Custom Vision inference run-time
     */
    public MSCognitiveServicesCustomVisionClassifier(final Context context) {
        // Tell Custom Vision inference run-time the application context so that it can load the model file
        CustomVisionManager.setAppContext(context);

        // Build a config object for ImageClassifier
        ImageClassifier.Configuration config = ImageClassifier.ConfigurationBuilder()
                .setModelFile(ModelManifestPath).build();

        // Instantiate an ImageClassifier
        classifierRuntime = new ImageClassifier(config);
    }

    @Override
    public Classifier.Recognition classifyImage(Bitmap sourceImage, int orientation) {
        // Rotate the image according to the given orientation
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        Bitmap rotatedBitmap = Bitmap.createBitmap(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), matrix, true);

        // Specify the image and run inference
        classifierRuntime.setImage(rotatedBitmap);
        classifierRuntime.run();

        // Recycle the bitmap now so that we won't use up memory across multiple calls to this method
        rotatedBitmap.recycle();

        // Print all the prediction results - for debugging purpose only
        final float[] confidences = classifierRuntime.Confidences.getFloatVector();
        final String[] labels = classifierRuntime.Identifiers.getStringVector();
        for (int i = 0; i < confidences.length; i++) {
            final float confidence = confidences[i];
            final String label = labels[i];
            Log.i(TAG, String.format("Confidence: %.1f (%s)", confidence * 100f, label));
        }

        // Results are sorted by confidence in descending order
        // Get the first item which represents highest confidence
        final float highestConfidence = confidences[0];
        final String labelForHighestConfidence = labels[0];

        // Return the label with the highest confidence
        return new Classifier.Recognition("0", labelForHighestConfidence, highestConfidence, null);
    }

    @Override
    public void close() {
        // Dispose the inference object to have resources cleaned up; e.g. recycle bitmaps
        classifierRuntime.close();
    }
}
