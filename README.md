---
services: cognitive-services,custom-vision
platforms: java, Android
author: aminbagheri
---

# Sample Android application for TensorFlow models exported from Custom Vision Service
This sample application demonstrates how to take a model exported from the [Custom Vision Service](https://www.customvision.ai) in the TensorFlow format and add it to an application for real-time image classification. 

## Getting Started

### Prerequisites
- [Android Studio (latest)](https://developer.android.com/studio/index.html)
- Android device
- An account at [Custom Vision Service](https://www.customvision.ai) 
### Quickstart

1. clone the repository and open the project in Android Studio
2. Build and run the sample on your Android device
### Replacing the sample model with your own classifier 
The model provided with the sample recognizes some fruits. to replace it with your own model exported from the [Custom Vision Service](https://www.customvision.ai) do the following, and then build and launch the application:
  1. [Create and train](https://docs.microsoft.com/en-us/azure/cognitive-services/custom-vision-service/getting-started-build-a-classifier) a classifer with the Custom Vision Service. You must choose a "compact" domain such as **General (compact)** to be able to export your classifier. If you have an existing classifier you want to export instead, convert the domain in "settings" by clicking on the gear icon at the top right. In setting, choose a "compact" model, Save, and Train your project.  
  2. Export your model by going to the Performance tab. Select an iteration trained with a compact domain, an "Export" button will appear. Click on *Export* then *TensorFlow* then *Export.* Click the *Download* button when it appears. A *.zip* file will download that contains TensorFlow model (.pb) and Labels (.txt)
  3. Drop your *model.pb* and *labels.txt* file into your Android project's Assets folder. 
  4. Build and run.

*This sample is tested on Pixel devices*

Make sure the mean values (IMAGE_MEAN_R, IMAGE_MEAN_G, IMAGE_MEAN_B in MSCognitiveServicesClassifier.java) are correct based on your project's domain in Custom Vision:

|  Project's Domain  | Mean Values (RGB) |
|--------------------|-------------------|
|  General (compact) |  (123, 117, 104)  |
|  Landmark (compact)|  (123, 117, 104)  |
|  Retail (compact)  |  (0, 0, 0)        |


## Resources
- Link to [TensorFlow documentation](https://www.tensorflow.org/mobile/)
- Link to [Custom Vision Service Documentation](https://docs.microsoft.com/en-us/azure/cognitive-services/custom-vision-service/home)

