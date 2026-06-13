# KaiSlideshow-Android

**Native Android implementation of KaiSlideshow** - A fullscreen image slideshow viewer with gesture controls and sharing support.

## 📱 Features

### ✅ Implemented and Working
- **Fullscreen Image Slideshow** - Auto-advancing image display with configurable interval
- **Gesture Controls**
  - Single tap: Play/Pause slideshow
  - Double tap: Toggle fullscreen mode
  - Swipe left: Next image
  - Swipe right: Previous image
- **Image Sources**
  - Add individual files via file picker
  - Add entire folders via folder picker
  - Open images from other apps (Share intent support)
  - Open multiple images from gallery
- **Playback Controls**
  - Configurable interval: 2, 3, 5, 10, 20, 30, 60, or 120 seconds
  - Shuffle mode: Random or sequential image order
  - Auto-hide UI: Controls fade out after 3 seconds of inactivity
- **Settings Persistence** - Interval and shuffle preferences saved between sessions
- **Dark Theme** - Optimized for image viewing in dark environments
- **Multiple Image Formats** - Supports JPG, JPEG, PNG, WEBP, BMP, GIF
- **Android Version Support** - Minimum SDK 24 (Android 7.0 Nougat)

### 🔄 Known Behavior
- **Screen Rotation**: Currently resets slideshow state (images may be lost on orientation change)
- **Pause State**: Control bar remains visible when paused (will auto-hide after timeout)
- **Swipe Gestures**: May temporarily show UI during swipe (UI auto-hides after timeout)

### 🚧 Planned Improvements
- [ ] Preserve slideshow state during screen rotation (using ViewModel)
- [ ] Auto-hide control bar during pause (while keeping pause overlay visible)
- [ ] Keep UI hidden during swipe gestures (only show on tap/double-tap)
- [ ] Add zoom/pan functionality for images
- [ ] Add transition animations between images
- [ ] Add slideshow speed control (separate from interval)
- [ ] Add image filtering options

## 📁 Project Structure

```
KaiSlideshow-Android/
├── app/
│   ├── src/main/java/com/kmrodni/kaislideshow/
│   │   ├── MainActivity.kt          # Main activity with slideshow logic
│   │   ├── KaiSlideshowApp.kt       # Application class
│   │   ├── models/
│   │   │   └── SlideshowSettings.kt  # Settings model with persistence
│   │   ├── utils/
│   │   │   ├── Constants.kt          # App constants and configuration
│   │   │   └── FileUtils.kt          # File operations and URI handling
│   │   └── views/
│   │       ├── ImageDisplayView.kt  # Custom view for image display (uses Glide)
│   │       ├── InfoBarView.kt        # Shows current image info (X/Y • filename)
│   │       └── ControlBarView.kt    # Control bar with settings and actions
│   │
│   └── src/main/res/
│       ├── layout/                  # XML layouts
│       │   ├── activity_main.xml     # Main activity layout
│       │   ├── view_control_bar.xml  # Control bar layout
│       │   └── view_info_bar.xml     # Info bar layout
│       ├── drawable/                # Drawable resources
│       │   ├── btn_rounded.xml       # Button background
│       │   ├── btn_exit_rounded.xml  # Exit button background
│       │   ├── control_bar_bg.xml    # Control bar background
│       │   ├── info_bar_bg.xml       # Info bar background
│       │   ├── pause_overlay_bg.xml  # Pause overlay background
│       │   ├── ic_launcher_background.xml  # App icon background
│       │   └── ic_launcher_foreground.xml  # App icon foreground
│       ├── values/                  # Resource values
│       │   ├── colors.xml            # Color definitions
│       │   ├── dimens.xml            # Dimension definitions
│       │   ├── strings.xml           # String resources
│       │   └── styles.xml            # Style definitions
│       ├── mipmap-*/               # App icons for different densities
│       └── xml/                     # XML resources
│           ├── backup_rules.xml     # Backup configuration
│           ├── data_extraction_rules.xml  # Data extraction rules
│           └── file_paths.xml       # FileProvider paths
│
├── build.gradle                    # Top-level build configuration
├── settings.gradle                 # Project settings and repositories
├── gradle.properties               # Gradle properties
├── gradlew                         # Gradle wrapper script (Unix)
├── gradlew.bat                     # Gradle wrapper script (Windows)
└── .gitignore                      # Git ignore rules
```

## 🛠 Setup

### Prerequisites
- Android Studio (latest version recommended)
- Java JDK 17 (included with Android Studio)
- Android SDK 34 (Android 14)
- Minimum SDK: 24 (Android 7.0 Nougat)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/kmrodni-boop/KaiSlideshow-Android.git
   cd KaiSlideshow-Android
   ```

2. **Open in Android Studio:**
   - File → Open... → Select the `KaiSlideshow-Android` folder
   - Android Studio will automatically sync Gradle

3. **Build the project:**
   - Click "Sync Now" when prompted
   - Wait for Gradle to download dependencies
   - Build → Make Project (or press Ctrl+F9 / Cmd+F9)

4. **Run the app:**
   - Connect an Android device (USB debugging enabled)
   - Or create an emulator (AVD Manager)
   - Click Run (▶️) button

## 📦 Dependencies

- **AndroidX Libraries** - Core Android components
- **Glide 4.16.0** - Image loading and caching
- **ConstraintLayout 2.1.4** - Flexible UI layouts
- **Activity Result API** - Modern file/folder picking

## 🎛 Usage

### Adding Images
- **Add Files**: Click the "Add Files" button to select individual image files
- **Add Folder**: Click the "Add Folder" button to select a folder containing images
- **Share from Gallery**: Select images in your gallery and share with KaiSlideshow
- **Open from File Manager**: Navigate to an image and open with KaiSlideshow

### Controls
| Action | Effect |
|--------|--------|
| Single tap | Play/Pause slideshow |
| Double tap | Toggle fullscreen |
| Swipe left | Next image |
| Swipe right | Previous image |
| Any touch (while playing) | Show UI temporarily |

### Settings
- **Interval**: Set time between images (2-120 seconds)
- **Shuffle**: Enable/disable random image order

## 📄 Intent Support

The app supports the following intents:
- `ACTION_VIEW` - Open a single image
- `ACTION_SEND` - Open a single image from share
- `ACTION_SEND_MULTIPLE` - Open multiple images from share
- `ACTION_GET_CONTENT` - Pick images from file manager
- `ACTION_OPEN_DOCUMENT_TREE` - Pick a folder

## 🔧 Build Configuration

```gradle
// build.gradle (top-level)
plugins {
    id 'com.android.application' version '8.2.2'
    id 'org.jetbrains.kotlin.android' version '1.9.20'
}

// app/build.gradle
android {
    namespace 'com.kmrodni.kaislideshow'
    compileSdk 34
    minSdk 24
    targetSdk 34
}

// settings.gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
```

## 📜 Permissions

The app requires the following permissions:
- `READ_EXTERNAL_STORAGE` - Read images from external storage (Android < 13)
- `READ_MEDIA_IMAGES` - Read media images (Android 13+)
- `READ_MEDIA_VISUAL_USER_SELECTED` - Read selected media (Android 13+)

## 🐛 Troubleshooting

### "Default activity not found"
- **Cause**: AndroidManifest.xml missing MAIN/LAUNCHER intent-filter
- **Fix**: Ensure `MainActivity` has the correct intent-filter in AndroidManifest.xml

### "Plugin not found"
- **Cause**: Gradle version mismatch or missing repositories
- **Fix**: Use Gradle 8.4 with AGP 8.2.2, ensure Google's Maven repository is in settings.gradle

### "Gradle JDK configuration"
- **Cause**: Wrong JDK selected
- **Fix**: In Android Studio: File → Settings → Build Tools → Gradle → Use Embedded JDK

### "Cannot resolve symbol"
- **Cause**: Missing dependencies or sync issue
- **Fix**: File → Sync Project with Gradle Files

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
