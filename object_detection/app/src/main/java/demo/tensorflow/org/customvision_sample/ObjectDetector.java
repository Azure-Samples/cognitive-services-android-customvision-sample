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

package demo.tensorflow.org.customvision_sample;

import android.graphics.Bitmap;
import android.graphics.RectF;
import java.util.List;

/**
 * Generic interface for interacting with different recognition engines.
 */
public interface ObjectDetector {
  /**
   * An immutable result returned by a Classifier describing what was recognized.
   */
  public static class BoundingBox {
    /**
     * Index of detected object class.
     */
    private Integer classIndex;

    /**
     * Class identifier.
     */
    private final String classIdentifier;

    /**
     * Confidence about detected object.
     */
    private final Float confidence;

    /** Optional location for the location of the detected object in normalized coodidate system. */
    private RectF location;

    public BoundingBox(
            Integer classIndex, final String title, final Float confidence, final RectF location) {
      this.classIndex = classIndex;
      this.classIdentifier = title;
      this.confidence = confidence;
      this.location = location;
    }

    public Integer getClassIndex() {
      return classIndex;
    }

    public String getClassIdentifier() {
      return classIdentifier;
    }

    public Float getConfidence() {
      return confidence;
    }

    public RectF getLocation() {
      return new RectF(location);
    }

    public void setLocation(RectF location) {
      this.location = location;
    }

    @Override
    public String toString() {
      String resultString = String.format("[%d] ", classIndex);

      if (classIdentifier != null) {
        resultString += classIdentifier + " ";
      }

      if (confidence != null) {
        resultString += String.format("(%.1f%%) ", confidence * 100.0f);
      }

      if (location != null) {
        resultString += location + " ";
      }

      return resultString.trim();
    }
  }

  List<BoundingBox> detectObjects(Bitmap bitmap, int orientation);
}
