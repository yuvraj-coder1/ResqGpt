package com.nervesparks.resqgpt

import android.app.DownloadManager
import android.content.ClipboardManager
import android.llama.cpp.LLamaAndroid
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import java.io.File
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nervesparks.resqgpt.data.UserPreferencesRepository

class MainViewModelFactory(
    private val llamaAndroid: LLamaAndroid,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(llamaAndroid, userPreferencesRepository) as T
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


    // Get a MemoryInfo object for the device's current memory status.
//    private fun availableMemory(): ActivityManager.MemoryInfo {
//        return ActivityManager.MemoryInfo().also { memoryInfo ->
//            activityManager.getMemoryInfo(memoryInfo)
//        }
//    }

    val darkNavyBlue = Color(0xFF001F3D) // Dark navy blue color
    val lightNavyBlue = Color(0xFF3A4C7C)



    val gradientBrush = Brush.verticalGradient(
        colors = listOf(darkNavyBlue, lightNavyBlue)
    )


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
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
        val viewModelFactory = MainViewModelFactory(lLamaAndroid, userPrefsRepo)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]


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









