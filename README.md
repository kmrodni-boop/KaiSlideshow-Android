# KaiSlideshow-Android

Android app for image slideshows - Native Android implementation of KaiSlideshow.

## Features

- Fullscreen image slideshow
- Support for multiple image formats (JPG, PNG, WEBP, BMP, GIF)
- Auto-advance with configurable interval (2-120 seconds)
- Shuffle mode
- Swipe gestures for navigation
- Tap to play/pause
- Double-tap to toggle fullscreen
- Add files or folders
- Share intent support (open images from other apps)
- Persistent settings
- Dark theme

## Project Structure

```
KaiSlideshow-Android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/kmrodni/kaislideshow/
│   │   │   │   ├── MainActivity.kt          # Main activity
│   │   │   │   ├── KaiSlideshowApp.kt       # Application class
│   │   │   │   ├── models/
│   │   │   │   │   └── SlideshowSettings.kt  # Settings model
│   │   │   │   ├── utils/
│   │   │   │   │   ├── Constants.kt          # App constants
│   │   │   │   │   └── FileUtils.kt          # File operations
│   │   │   │   └── views/
│   │   │   │       ├── ImageDisplayView.kt  # Image display view
│   │   │   │       ├── InfoBarView.kt        # Info bar view
│   │   │   │       └── ControlBarView.kt    # Control bar view
│   │   │   └── res/
│   │   │       ├── layout/                  # Layout files
│   │   │       ├── drawable/                # Drawable resources
│   │   │       ├── values/                  # String, color, style resources
│   │   │       └── xml/                      # XML resources
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Setup

1. Open in Android Studio
2. Sync Gradle
3. Build and run on device/emulator

## Dependencies

- [AndroidX](https://developer.android.com/jetpack/androidx) - Core Android libraries
- [Glide](https://github.com/bumptech/glide) - Image loading and caching
- [Dexter](https://github.com/Karumi/Dexter) - Runtime permissions
- [ConstraintLayout](https://developer.android.com/training/constraint-layout) - Flexible layouts

## Permissions

The app requires the following permissions:
- `READ_EXTERNAL_STORAGE` - To read images from external storage
- `READ_MEDIA_IMAGES` - To read media images (Android 13+)

## Usage

### Adding Images
- Click "Add Files" to select individual image files
- Click "Add Folder" to select a folder containing images
- Images can also be shared with the app from other apps (file managers, galleries, etc.)

### Controls
- **Single tap**: Play/Pause slideshow
- **Double tap**: Toggle fullscreen
- **Swipe left**: Next image
- **Swipe right**: Previous image
- **UI appears**: When slideshow is paused or on any interaction
- **UI hides**: After 3 seconds of inactivity during playback

### Settings
- **Interval**: Set the time between images (2-120 seconds)
- **Shuffle**: Enable/disable random image order

## Intent Support

The app supports the following intents:
- `ACTION_VIEW` - Open a single image
- `ACTION_SEND` - Open a single image from share
- `ACTION_SEND_MULTIPLE` - Open multiple images from share
- `ACTION_GET_CONTENT` - Pick images from file manager
- `ACTION_OPEN_DOCUMENT_TREE` - Pick a folder

## Build Configuration

- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## License

MIT License - see LICENSE file for details.
