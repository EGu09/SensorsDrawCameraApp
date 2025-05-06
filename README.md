# Sensor Draw Camera App

## Overview
This Android application explores Kotlin development with a focus on accessing native device features and APIs. The app consists of three main sections accessible through a bottom navigation bar: Sensors, Draw, and Camera.

## How It Works

### Sensors Tab
- Displays real-time data from three device sensors:
  - Magnetic Field Sensor: Shows the magnetic field strength in all three axes (X, Y, Z) and the total magnitude in microtesla (µT)
  - Proximity Sensor: Indicates how close an object is to the device's screen
  - Accelerometer: Shows acceleration forces along all three axes (X, Y, Z)
- Automatically detects available sensors and displays error messages when sensors are unavailable

### Draw Tab
- Allows users to select images from their device gallery
- Implements touch drawing functionality with proper coordinate translation to ensure accurate drawing
- Provides color selection options (black, red, green, blue)
- Includes image rotation correction using EXIF metadata to display images in their correct orientation
- Features a reset option to return to the original image
- Enables saving edited images to the device gallery as new files

### Camera Tab
- Provides access to both front and rear device cameras
- Displays a real-time camera preview for taking photos
- Includes a button to switch between front and back cameras
- Takes photos and saves them to the device gallery
- Displays the most recently captured photo

## Permissions Handling
- Implements runtime permission requests for camera and storage access
- Provides appropriate error messages when permissions are denied
- Handles different permission requirements based on Android version (particularly for Android 13+)

## Development Time
This project took approximately 20 hours to complete:
- Initial setup and research: 3 hours
- Sensors implementation: 4 hours
- Draw functionality: 6 hours
- Camera implementation: 5 hours
- Bug fixing and refinements: 2 hours

## Most Challenging Parts

1. **Camera Implementation**: Working with CameraX API required understanding lifecycle management and correct binding of camera use cases.

2. **Drawing Accuracy**: The most difficult part was ensuring that drawing on images was accurate on all devices. Translating touch coordinates to bitmap coordinates required careful calculation of scaling factors and offsets.

3. **Image Rotation**: Handling the automatic rotation of images based on EXIF metadata was challenging, especially ensuring the rotation was applied correctly before displaying images.

4. **Permission Management**: Adapting to the different permission models across Android versions (particularly the changes in Android 13 with the introduction of READ_MEDIA_IMAGES) required careful implementation.

## Resources Used

### Official Documentation
- Android Developers Documentation: https://developer.android.com/
- CameraX Documentation: https://developer.android.com/training/camerax
- Sensors Overview: https://developer.android.com/guide/topics/sensors/sensors_overview
- ExifInterface Documentation: https://developer.android.com/reference/androidx/exifinterface/media/ExifInterface

### Articles and Tutorials
- Medium article on Bottom Navigation Implementation: https://medium.com/@hitherejoe/exploring-the-android-design-support-library-bottom-navigation-drawer-548de699e8e0
- Stack Overflow thread on handling image rotation: https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a

### Libraries Used
- AndroidX Core: For core Android functionality
- AndroidX AppCompat: For backward compatibility
- Material Components: For Material Design UI elements
- CameraX: For camera functionality
- ExifInterface: For reading image metadata
