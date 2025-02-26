//package com.example.mycomposeapp
//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.Environment
//import android.provider.Settings
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import androidx.lifecycle.lifecycleScope
//import coil.compose.rememberAsyncImagePainter
//import coil.request.ImageRequest
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File

package com.example.mycomposeapp

//for permissions and file handling
import android.Manifest //request storage permissions
import android.content.Intent // Allows opening a file
import android.content.pm.PackageManager //to check if permission is granted
import android.net.Uri
import android.os.Build
import android.os.Bundle // Holds saved instance state data
import android.os.Environment //for access to external storage folders
import android.provider.Settings // to pen device settings for permission

import android.widget.Toast // to display short messages to the user

//Jetpack Compose activity setup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent


import androidx.activity.result.contract.ActivityResultContracts // for permission requests and activity results

//Jetpack Compose UI components and layouts
import androidx.compose.foundation.Image // to displays images
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // to makes UI elements clickable
import androidx.compose.foundation.layout.* // Provides layout elements like Row, Column, etc.
import androidx.compose.foundation.lazy.grid.GridCells // Defines grid structure (fixed or adaptive cells)
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid // to display items in a scrollable grid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape // gives circular shape for UI elements

//Jetpack Compose Material Icons
import androidx.compose.material.icons.Icons // Provides access to built-in Material icons
import androidx.compose.material.icons.filled.ArrowBack // Back navigation icon
import androidx.compose.material.icons.filled.Close // Close/cancel icon
import androidx.compose.material.icons.filled.PlayArrow // Play button icon (for video files)

// Jetpack Compose Material3 UI Components
import androidx.compose.material3.*
import androidx.compose.runtime.* // Enables state handling in Compose

// Jetpack Compose Utility Imports
import androidx.compose.ui.Alignment // Aligns UI components within layouts
import androidx.compose.ui.Modifier // Modifiesd UI elements (padding, size, background)
import androidx.compose.ui.layout.ContentScale // Defines how images fit inside their container
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign // Aligns text within a UI element
import androidx.compose.ui.unit.dp

// AndroidX Core Utilities
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

// Lifecycle and Coroutine Support
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//Coil - Image Loading Library
import coil.compose.rememberAsyncImagePainter // Loads images asynchronously in Compose
import coil.request.ImageRequest // Handles image requests

// Java File Handling
import java.io.File // Represents file paths and handles file operations

import android.util.Log // To display log statements


class MainActivity : ComponentActivity() {
    // State variables to manage app state
    private var permissionGranted by mutableStateOf(false)  // Tracks if storage permission is granted
    private var statusFiles by mutableStateOf<List<File>>(emptyList())  // List of status files found
    private var selectedMedia by mutableStateOf<File?>(null)  // Currently selected media for full screen view
    private var isLoading by mutableStateOf(false)  // Loading state for status files retrieval
    private var currentScreen by mutableStateOf(Screen.HOME)  // Current screen being displayed

    // Enum class to manage navigation between screens
    enum class Screen {
        HOME,
        STATUS_GRID,
        FULL_SCREEN
    }

    // Modern approach to handle activity results - used for Android 11+ permissions
    private val storageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Check if permission is granted after returning from Settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionGranted = Environment.isExternalStorageManager()
            val message = if (permissionGranted) {
                "All Files Access Permission Granted"
            } else {
                "All Files Access Permission Denied"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.d("PermissionFlow", "All Files Access Permission result: $permissionGranted")
        }
    }

    // Modern approach to handle permission requests - for Android 10 and below
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted = isGranted
        val message = if (isGranted) {
            "Storage Permission Granted"
        } else {
            "Storage Permission Denied"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d("PermissionFlow", "Storage Permission result: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ActivityLifecycle", "MainActivity onCreate called")

        // Check initial permission status
        permissionGranted = checkStoragePermission()
        Log.d("PermissionFlow", "Initial permission status: $permissionGranted")

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // handle navigation based on current screen state
                    when (currentScreen) {
                        Screen.HOME -> {
                            Log.d("Navigation", "Displaying HOME screen")
                            HomeScreen(
                                permissionGranted = permissionGranted,
                                onRequestPermission = { requestStoragePermission() },
                                onViewStatus = {
                                    if (permissionGranted) {
                                        Log.d("Navigation", "Permission granted, loading status files")
                                        loadWhatsAppStatusFiles()
                                        currentScreen = Screen.STATUS_GRID
                                    } else {
                                        Log.d("Navigation", "Permission denied for viewing status")
                                        Toast.makeText(
                                            this,
                                            "Please grant permission first",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                        Screen.STATUS_GRID -> {
                            Log.d("Navigation", "Displaying STATUS_GRID screen with ${statusFiles.size} files")
                            StatusGridScreen(
                                isLoading = isLoading,
                                statusFiles = statusFiles,
                                onBackPressed = {
                                    Log.d("Navigation", "Back pressed, returning to HOME")
                                    currentScreen = Screen.HOME
                                },
                                onMediaClick = { file ->
                                    Log.d("Navigation", "Media clicked: ${file.name}, going to FULL_SCREEN")
                                    selectedMedia = file
                                    currentScreen = Screen.FULL_SCREEN
                                }
                            )
                        }
                        Screen.FULL_SCREEN -> {
                            Log.d("Navigation", "Displaying FULL_SCREEN for media: ${selectedMedia?.name}")
                            selectedMedia?.let { file ->
                                FullScreenMediaView(
                                    file = file,
                                    onClose = {
                                        Log.d("Navigation", "Closing full screen view, returning to STATUS_GRID")
                                        currentScreen = Screen.STATUS_GRID
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the app has the required storage permissions based on Android version
     * Android 11+ requires MANAGE_EXTERNAL_STORAGE permission
     * Android 10 and below require READ_EXTERNAL_STORAGE permission
     */
    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above, check if MANAGE_EXTERNAL_STORAGE is granted
            val isGranted = Environment.isExternalStorageManager()
            Log.d("PermissionFlow", "Android 11+ permission check: $isGranted")
            isGranted
        } else {
            // For Android 10 and below, check READ_EXTERNAL_STORAGE permission
            val isGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            Log.d("PermissionFlow", "Android 10 and below permission check: $isGranted")
            isGranted
        }
    }

    /**
     * Request the appropriate storage permission based on Android version
     * Android 11+ requires opening system settings to grant permission
     * Android 10 and below use runtime permission request
     */
    private fun requestStoragePermission() {
        Log.d("PermissionFlow", "Requesting storage permission for SDK ${Build.VERSION.SDK_INT}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above, request MANAGE_EXTERNAL_STORAGE permission
            try {
                Log.d("PermissionFlow", "Requesting All Files Access via package specific intent")
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${applicationContext.packageName}")
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                // If the above intent fails, try the general settings page
                Log.d("PermissionFlow", "Package specific intent failed, using general settings: ${e.message}")
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            // For Android 10 and below, request standard storage permissions
            Log.d("PermissionFlow", "Requesting READ_EXTERNAL_STORAGE permission")
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Loads WhatsApp status files from various possible storage locations
     * Handles both WhatsApp and WhatsApp Business
     * Run in background thread to avoid UI blocking
     */
    private fun loadWhatsAppStatusFiles() {
        Log.d("FileLoading", "Starting to load WhatsApp status files")
        isLoading = true
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Different paths for WhatsApp and WhatsApp Business
                val paths = listOf(
                    // WhatsApp regular path
                    File(Environment.getExternalStorageDirectory().toString() + "/WhatsApp/Media/.Statuses"),
                    // Some devices store in Android/media path
                    File(Environment.getExternalStorageDirectory().toString() + "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
                    // WhatsApp Business path
                    File(Environment.getExternalStorageDirectory().toString() + "/WhatsApp Business/Media/.Statuses"),
                    // Some devices store WhatsApp Business in Android/media path
                    File(Environment.getExternalStorageDirectory().toString() + "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses")
                )

                val files = mutableListOf<File>()

                // Check each path for status files
                for (path in paths) {
                    Log.d("FileLoading", "Checking path: ${path.absolutePath}, exists: ${path.exists()}")
                    if (path.exists() && path.isDirectory) {
                        path.listFiles()?.let { dirFiles ->
                            val mediaFiles = dirFiles.filter {
                                it.isFile && (
                                        it.name.endsWith(".jpg", ignoreCase = true) ||
                                                it.name.endsWith(".jpeg", ignoreCase = true) ||
                                                it.name.endsWith(".png", ignoreCase = true) ||
                                                it.name.endsWith(".mp4", ignoreCase = true) ||
                                                it.name.endsWith(".gif", ignoreCase = true)
                                        )
                            }
                            Log.d("FileLoading", "Found ${mediaFiles.size} media files in ${path.absolutePath}")
                            files.addAll(mediaFiles)
                        }
                    }
                }

                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    statusFiles = files.sortedByDescending { it.lastModified() }
                    Log.d("FileLoading", "Total status files loaded: ${statusFiles.size}")
                    isLoading = false
                }
            } catch (e: Exception) {
                // Handle errors and update UI on main thread
                Log.e("FileLoading", "Error loading status files", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error loading status files: ${e.message}", Toast.LENGTH_LONG).show()
                    isLoading = false
                }
            }
        }
    }
}

/**
 * Home Screen composable - display welcome message and permission status
 * buttons to request permission and view status files
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onViewStatus: () -> Unit
) {
    Log.d("Composable", "HomeScreen rendered with permissionGranted: $permissionGranted")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CenterAlignedTopAppBar(
            title = { Text("WhatsApp Status Viewer") },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Welcome to ASHDETKON WhatsApp Status Viewer",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (permissionGranted)
                "Storage Permission: GRANTED"
            else
                "Storage Permission: DENIED",
            style = MaterialTheme.typography.bodyLarge,
            color = if (permissionGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "This app needs storage permission to access WhatsApp status files.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                Log.d("UserAction", "Permission request button clicked")
                onRequestPermission()
            },
            enabled = !permissionGranted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text(
                text = if (permissionGranted)
                    "Permission Already Granted"
                else
                    "Give Permission"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                Log.d("UserAction", "View Status button clicked")
                onViewStatus()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text(text = "View Status")
        }
    }
}

/**
 * Status Grid Screen composable - displays grid of status media files (grid of 3 row)
 * Show loading indicator while files are being loaded
 * Empty state shown when no files are found
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusGridScreen(
    isLoading: Boolean,
    statusFiles: List<File>,
    onBackPressed: () -> Unit,
    onMediaClick: (File) -> Unit
) {
    Log.d("Composable", "StatusGridScreen rendered with isLoading: $isLoading, filesCount: ${statusFiles.size}")
    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text("Status Gallery") },
            navigationIcon = {
                IconButton(onClick = {
                    Log.d("UserAction", "Back button clicked in StatusGridScreen")
                    onBackPressed()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        if (isLoading) {
            // Loading state
            Log.d("UIState", "Showing loading indicator in StatusGridScreen")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (statusFiles.isEmpty()) {
            // Empty state
            Log.d("UIState", "Showing empty state in StatusGridScreen")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No status files found. Make sure you've viewed status in WhatsApp.",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Grid of status files
            Log.d("UIState", "Showing grid with ${statusFiles.size} items")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(statusFiles) { file ->
                    StatusMediaGridItem(file = file, onMediaClick = onMediaClick)
                }
            }
        }
    }
}

/**
 * Status Media Grid Item composable - displays a single media item in the grid
 * Shows thumbnail for images and videos (with play icon on top for videos)
 */
@Composable
fun StatusMediaGridItem(file: File, onMediaClick: (File) -> Unit) {
    val context = LocalContext.current
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val isVideo = file.name.endsWith(".mp4", ignoreCase = true)
    Log.d("GridItem", "Rendering grid item for file: ${file.name}, isVideo: $isVideo")

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clickable {
                Log.d("UserAction", "Grid item clicked: ${file.name}")
                onMediaClick(file)
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .build()
            ),
            contentDescription = "Status media",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Add play icon overlay for video files
        if (isVideo) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Full Screen Media View composable - display selected media in full screen
 * Shows image directly or video thumbnail with play button that launches default video player
 */
@Composable
fun FullScreenMediaView(file: File, onClose: () -> Unit) {
    val context = LocalContext.current
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val isVideo = file.name.endsWith(".mp4", ignoreCase = true)
    Log.d("FullScreen", "Rendering full screen view for: ${file.name}, isVideo: $isVideo")

    Box(modifier = Modifier.fillMaxSize()) {
        if (isVideo) {
            // For videos let's just show a thumbnail with a play button that launches the user default video player
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .build()
                ),
                contentDescription = "Video thumbnail",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        Log.d("UserAction", "Video thumbnail clicked, launching video player for: ${file.name}")
                        // Open video with default video player
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(uri, "video/*")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(intent)
                    },
                contentScale = ContentScale.Fit
            )

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play Video",
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.Center),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            // For images, show full screen
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .build()
                ),
                contentDescription = "Status image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Close button
        IconButton(
            onClick = {
                Log.d("UserAction", "Full screen close button clicked")
                onClose()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}