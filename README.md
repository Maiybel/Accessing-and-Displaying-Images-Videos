# Permissions Handling, Accessing and Displaying Images/Videos(Storage Access for Whatsapp Status Saver)


An Android application that allows users to view their WhatsApp and WhatsApp Business status updates. Built with Jetpack Compose and Material 3 design principles.


## Features

- **View WhatsApp Status**: Browse through status updates you've viewed in both WhatsApp and WhatsApp Business
- **Image & Video Support**: View images and play videos from status updates
- **Material 3 Design**: Modern UI with Material You design language
- **Jetpack Compose**: Built entirely with Jetpack Compose for a fluid and responsive UI
- **Multi-Source Support**: Automatically detects status files from various storage locations
- **Adaptive Permissions**: Handles permission requirements for different Android versions (10, 11+)


## Requirements

- Android Studio Flamingo (2022.2.1) or newer
- Kotlin 1.8.0 or newer
- Minimum SDK: Android 6.0 (API level 23)
- Target SDK: Android 13 (API level 33)
- Gradle version: 8.0.0 or newer

## Installation

### Option 1: Download from Releases (Easiest)

1. Go to the [Releases](https://github.com/yourusername/whatsapp-status-viewer/releases) section
2. Download the latest APK file
3. Install it on your Android device (make sure to allow installation from unknown sources in settings)

### Option 2: Clone and Build

1. Clone this repository:
```bash
git clone https://github.com/yourusername/whatsapp-status-viewer.git
```
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run the app on your device or emulator:
```bash
./gradlew installDebug
```

### Option 3: Run via USB

1. Enable USB debugging on your Android device:
   - Go to **Settings** → **About phone** → Tap **Build number** 7 times to enable developer options
   - Return to **Settings** → **System** → **Developer options** → Enable **USB debugging**

2. Connect your device to your computer using a USB cable

3. In Android Studio:
   - Select your device from the target device dropdown menu in the toolbar
   - Click the "Run" button (green triangle) or press Shift+F10
   - The app will be installed and launched on your connected device

4. If your device isn't recognized:
   - Check that you've installed the appropriate USB drivers for your device
   - Try disconnecting and reconnecting the USB cable
   - Ensure you've allowed USB debugging on your device when prompted
  
     
## Setup Instructions
## First-time Setup
1. Install the app on your device
2. Open the app
3. Tap on "Give Permission" button
4. For Android 11 and above:
  - You'll be redirected to the system settings
  - Toggle "Allow access to manage all files" to ON
  - Return to the app
5. For Android 10 and below:
  - Grant the storage permission when prompted
6. After permission is granted, tap "View Status" to see your WhatsApp status files

## Permission Requirements
This app requires the following permissions:

- **For Android 11+**: MANAGE_EXTERNAL_STORAGE permission (requires user to go to Settings)
- **For Android 10 and below**: READ_EXTERNAL_STORAGE permission

## Viewing WhatsApp Status
For the app to display status updates, you must first:
1. Open your WhatsApp app
2. View the status updates of your contacts
3. Return to this app and tap "View Status"
The app will scan for status files in the following locations:

- /WhatsApp/Media/.Statuses
- /Android/media/com.whatsapp/WhatsApp/Media/.Statuses
- /WhatsApp Business/Media/.Statuses
- /Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses

## Troubleshooting
##No Status Files Found
If you see "No status files found" message:

1. Make sure you've granted all required permissions
2. Check that you've actually viewed status updates in WhatsApp recently
3. Some device manufacturers modify the WhatsApp storage path - if files aren't found, try viewing more statuses in WhatsApp
4. Restart the app after viewing statuses

## Permission Issues
For Android 11+ devices:

  - If the "All Files Access" permission page doesn't open, try using the "Give Permission" button again
  - If the setting still doesn't appear, you may need to manually go to:
    - Settings → Apps → WhatsApp Status Viewer → Permissions → Files and media → Allow management of all files

For Android 10 and below:

- If permission is denied repeatedly, go to:
    - Settings → Apps → WhatsApp Status Viewer → Permissions → Storage → Allow

## Video Playback Issues
If videos don't play:
- Ensure you have a video player installed on your device
- Try installing VLC for Android or MX Player
- Check if the video format is supported by your device

## Project Structure
```bash
app/
├── src/
│   ├── main/
│   │   ├── java/com/yourpackage/whatsappstatusviewer/
│   │   │   ├── MainActivity.kt  # Main activity with all screens and functionality
│   │   │   └── ...
│   │   ├── res/
│   │   │   ├── drawable/        # App icons and images
│   │   │   ├── values/          # Theme colors, strings, etc.
│   │   │   └── ...
│   │   └── AndroidManifest.xml  # App configuration
│   └── ...
├── build.gradle                  # App-level build configuration
└── ...
```

## Technical Details
## Key Components
### Screens

1. **HomeScreen**: Welcome screen with permission management
2. **StatusGridScreen**: Grid view of all status files
3. **FullScreenMediaView**: Full screen display for images and videos

## Permissions Handling
- Uses the latest Android permissions API
- Different flows for Android 11+ (MANAGE_EXTERNAL_STORAGE)
- Standard runtime permissions for older versions

## File Loading

- Background processing with Kotlin Coroutines
- Multiple path checking for different WhatsApp versions and device configurations

## Libraries Used

- **Jetpack Compose**: UI toolkit
- **Coil**: Image loading
AndroidX Lifecycle: Lifecycle-aware components
Material 3: Modern design components



Note: This app is not affiliated with, associated with, or endorsed by WhatsApp Inc. or Meta Platforms, Inc.
