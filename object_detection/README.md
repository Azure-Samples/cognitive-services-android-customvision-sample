---
services: cognitive-services,custom-vision
platforms: java, Android
author: kojiw
---

# Sample Android application for TensorFlow models exported from Custom Vision Service

This sample application demonstrates how to take a model exported from the [Custom Vision Service](https://www.customvision.ai) in the TensorFlow format and add it to an application for real-time object detection. 

## Getting Started

### Prerequisites

- [Android Studio (latest)](https://developer.android.com/studio/index.html)
- Android device
- An account at [Custom Vision Service](https://www.customvision.ai) 


### Quickstart

1. Clone the repository and open the project `object_detection` in Android Studio
2. Build and run the sample on your Android device


### Replacing the sample model with your own object detector
The model provided with the sample recognizes dogs and cats. To replace it with your own model exported from the [Custom Vision Service](https://www.customvision.ai) do the following, and then build and launch the application:

  1. [Create and train](https://docs.microsoft.com/en-us/azure/cognitive-services/custom-vision-service/get-started-build-detector) an object detector with the Custom Vision Service. You must choose a "compact" domain such as **General (compact)** to be able to export your object detector. If you have an existing object detector you want to export instead, convert the domain in "settings" by clicking on the gear icon at the top right. In setting, choose a "compact" model, Save, and Train your project.

  2. Export your model by going to the Performance tab. Select an iteration trained with a compact domain, an "Export" button will appear. Click on *Export* then *TensorFlow Lite* then *Export.* Click the *Download* button when it appears. A *.zip* file will download that contains all of these three files:
      - TensorFlow model (`.tflite`)
      - Labels (`.txt`)
      - Export manifest file (`cvexport.manifest`).

  3. Drop all of `model.tflite`, `labels.txt` and `cvexport.manifest` into your Android project's Assets folder.

  4. Build and run.

*This sample is tested on Pixel devices*


### Compatibility

This latest sample application relies on the new Android library *Custom Vision inference run-time* (or simply *run-time*) to take care of compatibility. It handles:

- __Subtract mean values__: Check if the exported model has normalization layer, and if not do this extra work - subtract mean values from RGB bytes of the input image before passing to TensorFlow inference engine. This applies only to old models exported before May 1, 2018. This has been done in the old implementation `MSCognitiveServicesClassifier.classifyImage` and is now encapsulated into the run-time.

- __Resize and crop input image__: Resize the image to a certain size and crop its center before passing to TensorFlow inference engine. The target size of the image is determined per given model, by looking into model's layers. This has been done in the old implementation `MSCognitiveServicesClassifier.cropAndRescaleBitmap` and is now encapsulated into the run-time

- __Version check__: Check the version of the exported model by looking at `cvexport.manifest` (more specifically, look for *ExporterVersion* field) and switch logic depending on model version.

    - __Fowrard compatibility__: It is when model version is newer than run-time's maximum supported model version.
    
        - Major version is greater: Throw exception (supposing model format is unknown)

        - Major version is same but minor version is greater: Still work. Run inference.

    - __Backward compatiblity__: Any newer version of the run-time should be able to handle older model versions.

#### Supported model versions

| Run-time version  | Model version |
|--:                |--             |
| Run-time 1.0.0    | Work with model version 1.x |
|                   | Work with model version 2.x |
|                   | Not work with model version 3.0 or higher |


## Resources
- Link to [TensorFlow documentation](https://www.tensorflow.org/mobile/)
- Link to [Custom Vision Service Documentation](https://docs.microsoft.com/en-us/azure/cognitive-services/custom-vision-service/home)
