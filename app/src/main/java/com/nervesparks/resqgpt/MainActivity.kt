package com.nervesparks.resqgpt

//import com.nervesparks.resqgpt.nearbyConnectionApi.CodenameGenerator
import android.Manifest
import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.llama.cpp.LLamaAndroid
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.nervesparks.resqgpt.data.UserPreferencesRepository
import java.io.File
import kotlin.random.Random

class MainViewModelFactory(
    private val llamaAndroid: LLamaAndroid,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(llamaAndroid, userPreferencesRepository, context = context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


class MainActivity(
//    activityManager: ActivityManager? = null,
    downloadManager: DownloadManager? = null,
    clipboardManager: ClipboardManager? = null,
): ComponentActivity() {
    //    private val tag: String? = this::class.simpleName
//
//    private val activityManager by lazy { activityManager ?: getSystemService<ActivityManager>()!! }
    private val downloadManager by lazy { downloadManager ?: getSystemService<DownloadManager>()!! }
    private val clipboardManager by lazy { clipboardManager ?: getSystemService<ClipboardManager>()!! }

    private lateinit var viewModel: MainViewModel
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    val darkNavyBlue = Color(0xFF001F3D) // Dark navy blue color
    val lightNavyBlue = Color(0xFF3A4C7C)

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(darkNavyBlue, lightNavyBlue)
    )
    private val STRATEGY = Strategy.P2P_STAR
    private lateinit var connectionsClient: ConnectionsClient
    private var opponentName: String? = null
    private var opponentEndpointId: String? = null
    private var myCodeName: String = Random.Default.nextInt(1000, 9999).toString()
    /** Callback for receiving arbitrary text messages (e.g. SOS signals). */
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // Convert incoming bytes to a UTF-8 string and show it
            payload.asBytes()?.let { bytes ->
                val message = String(bytes, Charsets.UTF_8) // message received

//                binding.status.text = "Received: $message"
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // If you need to know when the payload has fully arrived,
            // you can check here. For simple text, you often don’t need anything.
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                // Optionally log or toast that payload arrived successfully
                 Toast.makeText(this@MainActivity, "Message delivered", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Callbacks for connections to other devices
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Accepting a connection means you want to receive messages. Hence, the API expects
            // that you attach a PayloadCall to the acceptance
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            opponentName = "Opponent\n(${info.endpointName})"
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                opponentEndpointId = endpointId
                Toast.makeText(this@MainActivity, "Connected", Toast.LENGTH_SHORT).show()
//                setGameControllerEnabled(true) // we can start playing
            }
        }

        override fun onDisconnected(endpointId: String) {
//            resetGame()
            Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        connectionsClient = Nearby.getConnectionsClient(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.parseColor("#FF070915")//for status bar color

        StrictMode.setVmPolicy(
            VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build()
        )
        val userPrefsRepo = UserPreferencesRepository.getInstance(applicationContext)

        val lLamaAndroid = LLamaAndroid.instance()
        val viewModelFactory = MainViewModelFactory(lLamaAndroid, userPrefsRepo,this@MainActivity)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            val errMsg = "Cannot start without required permissions"
            if (allGranted) {
                recreate()
            } else {
                Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
                finish()
            }
        }

        if (!hasRequiredPermissions()) {
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            // Add Bluetooth permissions for Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }

            // Add NEARBY_WIFI_DEVICES permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }

            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }



//        val free = Formatter.formatFileSize(this, availableMemory().availMem)
//        val total = Formatter.formatFileSize(this, availableMemory().totalMem)
        val transparentColor = Color.Transparent.toArgb()
        window.decorView.rootView.setBackgroundColor(transparentColor)
//        viewModel.log("Current memory: $free / $total")
//        viewModel.log("Downloads directory: ${getExternalFilesDir(null)}")


        val extFilesDir = getExternalFilesDir(null)

        val models = listOf(
//            Downloadable(
//                "SmolLM-135M.Q2_K.gguf",
//                Uri.parse("https://huggingface.co/QuantFactory/SmolLM-135M-GGUF/resolve/main/SmolLM-135M.Q2_K.gguf?download=true"),
//                File(extFilesDir, "SmolLM-135M.Q2_K.gguf")
//
//            ),
//            Downloadable(
//                "Llama-3.2-3B-Instruct-Q4_K_L.gguf",
//                Uri.parse("https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_L.gguf?download=true"),
//                File(extFilesDir, "Llama-3.2-3B-Instruct-Q4_K_L.gguf")
//
//            ),
//            Downloadable(
//                "Llama-3.2-1B-Instruct-Q6_K_L.gguf",
//                Uri.parse("https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q6_K_L.gguf?download=true"),
//                File(extFilesDir, "Llama-3.2-1B-Instruct-Q6_K_L.gguf")
//            ),
            Downloadable(
                "stablelm-2-1_6b-chat.Q4_K_M.imx.gguf",
                Uri.parse("https://huggingface.co/Crataco/stablelm-2-1_6b-chat-imatrix-GGUF/resolve/main/stablelm-2-1_6b-chat.Q4_K_M.imx.gguf?download=true"),
                File(extFilesDir, "stablelm-2-1_6b-chat.Q4_K_M.imx.gguf")
            )
        )

        if (extFilesDir != null) {
            viewModel.loadExistingModels(extFilesDir)
        }



        setContent {


            var showSettingSheet by remember { mutableStateOf(false) }
            var isBottomSheetVisible by rememberSaveable  { mutableStateOf(false) }
            var modelData by rememberSaveable  { mutableStateOf<List<Map<String, String>>?>(null) }
            var selectedModel by remember { mutableStateOf<String?>(null) }
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            val sheetState = rememberModalBottomSheetState()

            var UserGivenModel by remember {
                mutableStateOf(
                    TextFieldValue(
                        text = viewModel.userGivenModel,
                        selection = TextRange(viewModel.userGivenModel.length) // Ensure cursor starts at the end
                    )
                )
            }
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
//            )

            ChatScreen(
                viewModel,
                clipboardManager,
                downloadManager,
                models,
                extFilesDir,
            )

        }
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
    }

    private fun hasRequiredPermissions(): Boolean {
        // Basic location permission check
        val hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        // Check Bluetooth permissions on Android 12+
        val hasBluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not needed on older Android versions
        }

        // Check NEARBY_WIFI_DEVICES permission on Android 13+
        val hasNearbyWifiPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not needed on older Android versions
        }

        return hasLocationPermission && hasBluetoothPermissions && hasNearbyWifiPermission
    }


    private fun sendMessage(message: String) {
        // Convert your String into a Payload
        val payload = Payload.fromBytes(message.toByteArray(Charsets.UTF_8))

        // Send it over the established Nearby Connections channel
        connectionsClient.sendPayload(
            opponentEndpointId ?: return,  // safety check
            payload
        )

        // Update your UI so the user knows it was sent

    }
    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startAdvertising(
            myCodeName,
            packageName,
            connectionLifecycleCallback,
            options
        )
    }
    // Callbacks for finding other devices
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(myCodeName, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
        }
    }
    private fun startDiscovery(){
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(packageName,endpointDiscoveryCallback,options)
    }
}

@Composable
fun LinearGradient() {
    val darkNavyBlue = Color(0xFF050a14)
    val lightNavyBlue = Color(0xFF051633)
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF)),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )





    Box(modifier = Modifier.background(gradient).fillMaxSize())
}







// [END android_compose_layout_material_modal_drawer]









