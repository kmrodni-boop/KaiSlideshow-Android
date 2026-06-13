# KaiSlideshow-Android

**Native Android implementation of KaiSlideshow** - A fullscreen image slideshow viewer with advanced gesture controls, zoom/pan functionality, and sharing support.

## 📱 Features

### ✅ Implemented and Working

#### Core Slideshow
- **Fullscreen Image Slideshow** - Auto-advancing image display with configurable interval
- **State Preservation** - **Remembers current image, position, and play state during screen rotation**
- **Multiple Image Formats** - Supports JPG, JPEG, PNG, WEBP, BMP, GIF
- **Settings Persistence** - All preferences saved between sessions
- **Dark Theme** - Optimized for image viewing in dark environments

#### Gesture Controls
- **Single tap** - Show/hide UI controls
- **Double tap** - Toggle play/pause
- **Long press** - Toggle fullscreen mode
- **Swipe left** - Next image (resets zoom)
- **Swipe right** - Previous image (resets zoom)
- **Pan/Drag** - Move zoomed images around (only available when paused)

#### Image Navigation
- **Previous/Next Buttons** - **Visible only when paused**, hidden during playback
- **Zoom Controls** - **Zoom in/out buttons visible only when paused**
- **Auto-reset** - Zoom **automatically resets** when navigating to next/previous image
- **Swipe Deactivation** - **Swipe gestures are disabled during zoom** to allow panning

#### Image Display
- **Glide Integration** - Smooth image loading and caching
- **Fade Transitions** - Configurable cross-fade between images
- **Ken Burns Effect** - Slow zoom/pan effect on images (configurable)
- **Manual Zoom** - 5 zoom levels (1x, 1.5x, 2x, 3x, 4x)
- **Pan Support** - Drag to move zoomed images (only when paused)

#### File Management
- **Add Files** - Select individual image files via system picker
- **Add Folder** - Select entire folder via document tree picker
- **Share from Gallery** - Open images from gallery share dialog
- **Open from File Manager** - Open images directly from file manager
- **Multiple Images** - Support for sharing multiple images at once
- **URI Handling** - Works with both file:// and content:// URIs

#### Settings (Accessible via Settings button)
- **Interval** - Time between images: 2, 3, 5, 10, 20, 30, 60, or 120 seconds
- **Shuffle** - Random or sequential image order
- **Fade Transitions** - Enable/disable smooth transitions between images
- **Ken Burns Effect** - Enable/disable slow zoom/pan effect
- **Sleep Timer** - Auto-exit after: Off, 15 min, 30 min, 1 hour, 2 hours, 4 hours

#### UI/UX
- **Auto-hide UI** - Controls fade out after 3 seconds of inactivity
- **Pause Overlay** - Small pause icon visible when slideshow is paused
- **Empty State** - Helpful message with quick actions when no images loaded
- **Material Design** - Modern UI with Material 3 components
- **Horizontal Scrolling Control Bar** - All controls accessible via swipe
- **Navigation Buttons** - Previous/Next and Zoom buttons **appear when paused**

#### Additional Features
- **Donate Button** - Link to PayPal for supporting development
- **Exit Confirmation** - Graceful exit with UI visibility
- **Error Handling** - Shows error messages for failed image loads
- **Loading Indicators** - Visual feedback during file operations

### 🔄 Current Behavior

#### ✅ Working as Designed
- **Screen Rotation**: **FULLY WORKING** - App now remembers:
  - Current image index
  - Play/pause state
  - Zoom level
  - All settings
- **Pause Mode**: 
  - Control bar **remains visible** when paused
  - **Zoom controls (in/out buttons) appear** when paused
  - **Navigation buttons (prev/next) appear** when paused
  - **Swipe gestures are disabled** during zoom to allow panning
- **Zoom Functionality**:
  - **Only available when paused** (not during playback)
  - **Pinch-to-zoom removed** (simplified for better image display)
  - **Manual zoom buttons** (in/out) used instead
  - **Swipe navigation resets zoom** to show full image
  - **Pan enabled** when zoomed (drag to move image)
- **UI Visibility**: 
  - UI auto-hides after 3 seconds of inactivity **during playback**
  - UI **stays visible** when paused (for easy navigation)

#### ⚠️ Known Limitations
- **Large Folders**: May take time to load (no progress indication for folder scanning)
- **Memory**: Very large images may cause memory issues (Glide handles most cases well)
- **Pinch-to-Zoom**: **Intentionally removed** - Simplified to manual zoom buttons for better image display consistency

### 🚧 Planned Improvements

- [ ] **Progress indicator for folder loading** - Show loading progress when scanning large folders
- [ ] **Image caching optimization** - Better memory management for large image collections
- [ ] **Custom transitions** - More transition types (slide, zoom, fade+zoom, etc.)
- [ ] **Image rotation** - Support for rotating images 90° left/right
- [ ] **Slideshow speed control** - Separate speed from interval for smoother transitions
- [ ] **Image filtering** - Apply filters (grayscale, sepia, etc.)
- [ ] **Playlists** - Save and load image collections
- [ ] **Favorites** - Mark favorite images
- [ ] **Thumbnails** - Show thumbnail grid for navigation

## 📁 Project Structure

```
KaiSlideshow-Android/
├── app/
│   ├── src/main/java/com/kmrodni/kaislideshow/
│   │   ├── MainActivity.kt              # Main activity with all logic
│   │   │                                   # Handles: rotation state, gestures,
│   │   │                                   # zoom/pan, settings, file picking
│   │   ├── KaiSlideshowApp.kt           # Application class
│   │   ├── models/
│   │   │   └── SlideshowSettings.kt      # Settings model with persistence
│   │   │                                   # Includes: interval, shuffle, transition,
│   │   │                                   # kenBurns, sleepTimer
│   │   ├── utils/
│   │   │   ├── Constants.kt              # App constants and configuration
│   │   │   └── FileUtils.kt              # File operations and URI handling
│   │   └── views/
│   │       ├── ImageDisplayView.kt      # Custom view with zoom/pan/Ken Burns
│   │       │                                   # Note: Manual zoom only (no pinch)
│   │       ├── InfoBarView.kt            # Shows current image info (X/Y • filename)
│   │       └── ControlBarView.kt        # Control bar with all action buttons
│   │
│   └── src/main/res/
│       ├── layout/                      # XML layouts
│       │   ├── activity_main.xml         # Main activity layout
│       │   │                                   # Includes: zoom buttons, nav buttons
│       │   ├── view_control_bar.xml     # Control bar with Material buttons
│       │   ├── view_info_bar.xml         # Info bar layout
│       │   └── dialog_settings.xml       # Settings dialog layout
│       ├── drawable/                    # Drawable resources
│       │   ├── btn_rounded.xml           # Button background
│       │   ├── btn_exit_rounded.xml      # Exit button background
│       │   ├── control_bar_bg.xml        # Control bar background
│       │   ├── info_bar_bg.xml           # Info bar background
│       │   ├── nav_button_bg.xml         # Navigation button background
│       │   ├── pause_overlay_bg.xml      # Pause overlay background
│       │   ├── ic_add.xml                # Add icon
│       │   ├── ic_exit.xml               # Exit icon
│       │   ├── ic_folder.xml             # Folder icon
│       │   ├── ic_heart.xml              # Heart/donate icon
│       │   ├── ic_settings.xml           # Settings icon
│       │   ├── ic_zoom_in.xml            # Zoom in icon
│       │   ├── ic_zoom_out.xml           # Zoom out icon
│       │   ├── ic_launcher_background.xml # App icon background
│       │   └── ic_launcher_foreground.xml # App icon foreground
│       ├── values/                      # Resource values
│       │   ├── colors.xml                # Color definitions
│       │   ├── dimens.xml                # Dimension definitions
│       │   ├── strings.xml               # All string resources
│       │   └── styles.xml                # Style definitions
│       ├── mipmap-*/                   # App icons for different densities
│       └── xml/                         # XML resources
│           ├── backup_rules.xml         # Backup configuration
│           ├── data_extraction_rules.xml # Data extraction rules
│           └── file_paths.xml           # FileProvider paths
│
├── build.gradle                        # Top-level build configuration
├── settings.gradle                     # Project settings and repositories
├── gradle.properties                   # Gradle properties
├── gradlew                             # Gradle wrapper script (Unix)
├── gradlew.bat                         # Gradle wrapper script (Windows)
├── gradle/wrapper/                    # Gradle wrapper files
│   ├── gradle-wrapper.jar             # Gradle wrapper JAR
│   └── gradle-wrapper.properties       # Gradle version configuration
└── .gitignore                          # Git ignore rules
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
- **Material Components** - Modern UI elements
- **Activity Result API** - Modern file/folder picking

## 🎛 Usage

### Adding Images
| Method | Description |
|--------|-------------|
| **Add Files** | Click the "Add Files" button to select individual image files |
| **Add Folder** | Click the "Add Folder" button to select a folder containing images |
| **Share from Gallery** | Select images in your gallery and share with KaiSlideshow |
| **Open from File Manager** | Navigate to an image and open with KaiSlideshow |

### Controls
| Action | Effect | Available When |
|--------|--------|----------------|
| Single tap | Show/hide UI controls | Always |
| Double tap | Toggle play/pause | Always |
| Long press | Toggle fullscreen mode | Always |
| Swipe left | Next image (resets zoom) | Always |
| Swipe right | Previous image (resets zoom) | Always |
| Drag/Pan | Move zoomed image | **Paused + Zoomed** |

### Navigation & Zoom Buttons (Visible when Paused)
| Button | Action | Effect |
|--------|--------|--------|
| ← | Previous | Go to previous image, reset zoom |
| → | Next | Go to next image, reset zoom |
| + | Zoom In | Increase zoom level |
| - | Zoom Out | Decrease zoom level |

**Note:** These buttons **only appear when slideshow is paused** and **disappear during playback**.

### Settings
Access settings via the **Settings** button in the control bar:
- **Interval**: Time between images (2-120 seconds)
- **Shuffle**: Random or sequential image order
- **Fade Transitions**: Enable smooth transitions between images
- **Ken Burns Effect**: Enable slow zoom/pan effect on images
- **Sleep Timer**: Auto-exit after selected time (Off, 15 min, 30 min, 1 hour, 2 hours, 4 hours)

### Control Bar Buttons
| Button | Icon | Action |
|--------|------|--------|
| Add Files | + | Select individual image files |
| Add Folder | 📁 | Select a folder of images |
| Settings | ⚙️ | Open settings dialog |
| Donate | ❤️ | Support development (PayPal) |
| Exit | ❌ | Exit the application |

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
    
    buildFeatures {
        viewBinding true
    }
}

// settings.gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

## 📜 Permissions

The app requires the following permissions:
- `READ_EXTERNAL_STORAGE` - Read images from external storage (Android < 13)
- `READ_MEDIA_IMAGES` - Read media images (Android 13+)
- `READ_MEDIA_VIDEO` - Read media video (for potential video support)
- `READ_MEDIA_VISUAL_USER_SELECTED` - Read selected media (Android 13+)

## 🎨 UI Features

### Visual Elements
- **Pause Overlay**: Small semi-transparent overlay in bottom-right corner when paused
- **Info Bar**: Shows current position (X/Y) and filename at bottom-left
- **Control Bar**: Horizontal scrolling bar at top with all action buttons
- **Navigation Buttons**: Previous/Next and Zoom In/Out buttons (**visible only when paused**)
- **Loading Indicator**: Centered progress spinner during file operations
- **Empty State**: Helpful screen with quick actions when no images are loaded

### Animations
- **Fade Transitions**: Smooth cross-fade between images (configurable)
- **Ken Burns Effect**: Slow zoom/pan animation on images (configurable)
- **Zoom Animations**: Smooth zoom in/out transitions
- **UI Fade**: Controls fade in/out smoothly

## 🎯 Design Decisions

### Zoom Implementation
- **Manual Zoom Only**: Pinch-to-zoom was **intentionally removed** to simplify the user experience
- **Reason**: Pinch-to-zoom added complexity that made it difficult to display images consistently during normal playback
- **Benefit**: Better control over image display, especially for images that are too large or too small
- **Alternative**: Manual zoom buttons (in/out) provide the same functionality without the complexity

### Zoom Behavior
- **Only Available When Paused**: Zoom functionality is disabled during playback
- **Reason**: Prevents accidental zooming during slideshow viewing
- **Swipe Deactivation**: Swipe gestures are disabled during zoom to allow panning
- **Auto-Reset**: Zoom automatically resets when navigating to next/previous image
- **Navigation Buttons**: Previous/Next buttons appear when paused for easy navigation

### Screen Rotation
- **Full State Preservation**: App now remembers all state during screen rotation
- **Implementation**: Uses Android's built-in activity lifecycle with proper state saving
- **Benefit**: Seamless experience when rotating device between portrait and landscape

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

### "Images not loading"
- **Cause**: Missing permissions or URI handling issue
- **Fix**: Ensure READ_MEDIA_IMAGES permission is granted, check URI handling in FileUtils

## 📊 Technical Details

### Architecture
- **Single Activity**: All functionality in MainActivity
- **Custom Views**: ImageDisplayView, InfoBarView, ControlBarView
- **State Management**: Activity handles state with proper lifecycle management
- **Activity Result API**: Modern approach for file/folder picking
- **Glide**: Image loading with caching and transitions

### Key Components
- **ImageDisplayView**: Handles image loading, manual zoom, pan, Ken Burns effect
- **MainActivity**: Manages slideshow logic, gestures, settings, state preservation
- **SlideshowSettings**: Data class for persistent settings
- **FileUtils**: URI handling and file operations

### Zoom Implementation Details
- **Zoom Levels**: 5 discrete levels (1x, 1.5x, 2x, 3x, 4x)
- **Zoom Controls**: Manual buttons (no pinch-to-zoom)
- **Pan Support**: Drag to move when zoomed (only when paused)
- **Swipe Behavior**: Swipe disabled during zoom to prevent accidental navigation
- **Navigation Buttons**: Appear when paused for easy browsing

## 📈 Performance Considerations

- **Image Loading**: Glide handles caching and memory management
- **Large Folders**: May take time to load (consider background loading)
- **Zoom/Pan**: Smooth animations with hardware acceleration
- **Memory**: Images are loaded at appropriate resolution for display
- **State Preservation**: Minimal overhead for rotation state handling

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Areas for Contribution
- Progress indicator for folder loading
- Additional transition effects
- Image editing features
- Performance optimizations
- UI improvements
- New features (playlists, favorites, etc.)
