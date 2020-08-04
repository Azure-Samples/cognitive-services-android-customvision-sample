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
import android.graphics.RectF;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import ai.customvision.CustomVisionManager;
//import ai.customvision.tfnormal.ObjectDetector; // TensorFlow normal
import ai.customvision.tflite.ObjectDetector; // TensorFlow Lite

import demo.tensorflow.org.customvision_sample.ObjectDetector.BoundingBox;

public class MSCognitiveServicesCustomVisionObjectDetector implements demo.tensorflow.org.customvision_sample.ObjectDetector, AutoCloseable {
    private static String TAG = MSCognitiveServicesCustomVisionObjectDetector.class.getSimpleName();

    // Specify the path of manifest file about the model to use
    // private static String ModelManifestPath = "sample.cvmodel/cvexport.manifest";  // TensorFlow model (.pb)
    private static String ModelManifestPath = "sample-tflite.cvmodel/cvexport.manifest";  // TensorFlow Lite model (.tflite)

    /**
     * ImageClassifier instance responsible to run inference
     */
    private ObjectDetector cvsObjectDetector;
    List<String> supportedIdentifiers;

    /**
     * Initialize an instance of this wrapper class to run inference.
     * @param context Application context to load model file from Custom Vision inference run-time
     */
    public MSCognitiveServicesCustomVisionObjectDetector(final Context context) {
        // Tell Custom Vision inference run-time the application context so that it can load the model file
        CustomVisionManager.setAppContext(context);

        // Build a config object for ImageClassifier
        ObjectDetector.Configuration config = ObjectDetector.ConfigurationBuilder()
                .setModelFile(ModelManifestPath).build();

        // Remember supported identifiers to get index of class
        supportedIdentifiers = Arrays.asList(config.SupportedIdentifiers.getStringVector());

        // Instantiate an ImageClassifier
        cvsObjectDetector = new ObjectDetector(config);
    }

    @Override
    public List<BoundingBox> detectObjects(Bitmap sourceImage, int orientation) {
        // Rotate the image according to the given orientation
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        Bitmap rotatedBitmap = Bitmap.createBitmap(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), matrix, true);

        // Specify the image and run inference
        cvsObjectDetector.setImage(rotatedBitmap);
        cvsObjectDetector.run();

        // Recycle the bitmap now so that we won't use up memory across multiple calls to this method
        rotatedBitmap.recycle();

        Log.i(TAG, String.format("Time taken ObjectDetector.run: %.0f", cvsObjectDetector.TimeInMilliseconds.getFloat()));

        // Build BoundingBox list about detected objects
        List<demo.tensorflow.org.customvision_sample.ObjectDetector.BoundingBox> results = null;

        final String[] labels = cvsObjectDetector.Identifiers.getStringVector();
        if (labels.length != 0) {
            results = new ArrayList<BoundingBox>();

            final int[] indexes = cvsObjectDetector.IdentifierIndexes.getIntVector();
            final float[] confidences = cvsObjectDetector.Confidences.getFloatVector();
            final RectF[] boundingBoxes = cvsObjectDetector.BoundingBoxes.getRectangleVector();
            for (int i = 0; i < confidences.length; i++) {
                final String label = labels[i];
                final float confidence = confidences[i];
                final RectF location = boundingBoxes[i];
                final int classIndex = indexes[i];

                // Print all the prediction results - for debugging purpose only
                Log.i(TAG, String.format("Label: %s, Confidence: %.1f, Rect: (%.1f, %.1f, %.1f, %.1f)", label, confidence, location.left, location.top, location.right, location.bottom));

                results.add(new BoundingBox(classIndex, label, confidence, location));
            }
        }
        return results;
    }

    @Override
    public void close() {
        // Dispose the inference object to have resources cleaned up; e.g. recycle bitmaps
        cvsObjectDetector.close();
    }
}
