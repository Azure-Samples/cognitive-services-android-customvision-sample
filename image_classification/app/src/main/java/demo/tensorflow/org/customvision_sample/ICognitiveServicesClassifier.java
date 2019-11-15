package demo.tensorflow.org.customvision_sample;

import android.graphics.Bitmap;

public interface ICognitiveServicesClassifier {
    Classifier.Recognition classifyImage(Bitmap sourceImage, int orientation);
    void close();
}
