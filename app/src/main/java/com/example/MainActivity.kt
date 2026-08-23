package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OnBackgroundDark
import com.example.ui.theme.OnPrimaryContainerPurple
import com.example.ui.theme.OnPrimaryPurple
import com.example.ui.theme.OnSecondaryContainerDark
import com.example.ui.theme.OnSurfaceVariantMuted
import com.example.ui.theme.OutlineColor
import com.example.ui.theme.PrimaryContainerLavender
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.ProgressTrackColor
import com.example.ui.theme.SecondaryContainerLight
import com.example.ui.theme.SelectedPillLavender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BuildHistoryItem(
  val id: String,
  val projectName: String,
  val timestamp: String,
  val status: String,
  val linesOfCode: Int
)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AiArchitectApp()
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    style = MaterialTheme.typography.headlineMedium,
    color = MaterialTheme.colorScheme.onBackground,
    modifier = modifier.testTag("greeting_text")
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiArchitectApp() {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var selectedTab by remember { mutableIntStateOf(0) }
  var showMenu by remember { mutableStateOf(false) }
  var showUploadDialog by remember { mutableStateOf(false) }
  var showSourceCodeDialog by remember { mutableStateOf(false) }
  var showLogViewerDialog by remember { mutableStateOf(false) }

  var currentProjectName by remember { mutableStateOf("Project_Source.zip") }
  var uploadProgress by remember { mutableFloatStateOf(0f) }
  var uploadStatus by remember { mutableStateOf("Menunggu Unggahan...") }
  var isProcessing by remember { mutableStateOf(false) }
  var customCodeInput by remember {
    mutableStateOf(
      """// Contoh Source Code Kotlin
package com.example.app

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun SimpleCounterApp() {
    var count by remember { mutableIntStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: ${'$'}count")
    }
}"""
    )
  }

  val buildLogs = remember {
    mutableStateListOf(
      "Inisialisasi parser AST...",
      "Membaca dependensi Gradle...",
      "Memvalidasi struktur Jetpack Compose..."
    )
  }

  val historyList = remember {
    mutableStateListOf(
      BuildHistoryItem("1", "Kalkulator_Modern.kt", "2026-08-23 09:15", "Berhasil (APK Siap)", 148),
      BuildHistoryItem("2", "Todo_List_Room.zip", "2026-08-22 18:40", "Berhasil (APK Siap)", 380),
      BuildHistoryItem("3", "Weather_Widget_M3.kt", "2026-08-21 14:20", "Berhasil (APK Siap)", 210)
    )
  }

  val animatedProgress by animateFloatAsState(
    targetValue = uploadProgress,
    animationSpec = tween(durationMillis = 300),
    label = "progress_animation"
  )

  fun startBuildPipeline(projectName: String) {
    currentProjectName = projectName
    isProcessing = true
    uploadProgress = 0.1f
    uploadStatus = "Mengunggah kode sumber..."

    coroutineScope.launch {
      buildLogs.clear()
      buildLogs.add("Memulai analisis: $projectName")
      delay(600)
      uploadProgress = 0.35f
      uploadStatus = "Menganalisis dependensi & logika..."
      buildLogs.add("Parser AST berhasil mengidentifikasi 4 modul UI.")
      delay(800)
      uploadProgress = 0.7f
      uploadStatus = "Mengompilasi DEX dan resource XML..."
      buildLogs.add("Optimalisasi Material 3 Theme: Warna & Tipografi tervalidasi.")
      delay(800)
      uploadProgress = 1.0f
      uploadStatus = "APK Siap Diunduh!"
      buildLogs.add("Build sukses: APK terverifikasi dan siap diinstal.")
      isProcessing = false

      historyList.add(
        0,
        BuildHistoryItem(
          id = System.currentTimeMillis().toString(),
          projectName = projectName,
          timestamp = "Baru saja",
          status = "Berhasil (APK Siap)",
          linesOfCode = customCodeInput.lines().size
        )
      )
      Toast.makeText(context, "Kompilasi '$projectName' berhasil!", Toast.LENGTH_SHORT).show()
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = BackgroundLight,
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = BackgroundLight,
          titleContentColor = OnBackgroundDark,
          navigationIconContentColor = OnBackgroundDark,
          actionIconContentColor = OnBackgroundDark
        ),
        title = {
          Text(
            text = "AI Architect",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Medium,
              letterSpacing = (-0.2).sp
            ),
            modifier = Modifier.testTag("app_title")
          )
        },
        navigationIcon = {
          IconButton(
            onClick = {
              if (selectedTab != 0) {
                selectedTab = 0
              } else {
                Toast.makeText(context, "Navigasi kembali", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier
              .testTag("back_button")
              .minimumInteractiveComponentSize()
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali"
            )
          }
        },
        actions = {
          Box {
            IconButton(
              onClick = { showMenu = !showMenu },
              modifier = Modifier
                .testTag("more_menu_button")
                .minimumInteractiveComponentSize()
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Opsi Lainnya"
              )
            }

            DropdownMenu(
              expanded = showMenu,
              onDismissRequest = { showMenu = false },
              modifier = Modifier.background(SecondaryContainerLight)
            ) {
              DropdownMenuItem(
                text = { Text("Log Kompilasi") },
                onClick = {
                  showMenu = false
                  showLogViewerDialog = true
                }
              )
              DropdownMenuItem(
                text = { Text("Paste Source Code") },
                onClick = {
                  showMenu = false
                  showSourceCodeDialog = true
                }
              )
              DropdownMenuItem(
                text = { Text("Tentang AI Architect") },
                onClick = {
                  showMenu = false
                  Toast.makeText(
                    context,
                    "AI Architect v1.0 - Bold Typography Edition",
                    Toast.LENGTH_LONG
                  ).show()
                }
              )
            }
          }
        }
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = SecondaryContainerLight,
        contentColor = OnSecondaryContainerDark,
        modifier = Modifier
          .border(0.5.dp, OutlineColor.copy(alpha = 0.5f))
          .navigationBarsPadding()
          .testTag("bottom_nav_bar")
      ) {
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
              contentDescription = "Beranda"
            )
          },
          label = {
            Text(
              text = "Beranda",
              style = MaterialTheme.typography.labelSmall
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = SelectedPillLavender,
            selectedIconColor = OnSecondaryContainerDark,
            selectedTextColor = OnSecondaryContainerDark,
            unselectedIconColor = OnSurfaceVariantMuted,
            unselectedTextColor = OnSurfaceVariantMuted
          )
        )

        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 1) Icons.Filled.History else Icons.Outlined.History,
              contentDescription = "Riwayat"
            )
          },
          label = {
            Text(
              text = "Riwayat",
              style = MaterialTheme.typography.labelSmall
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = SelectedPillLavender,
            selectedIconColor = OnSecondaryContainerDark,
            selectedTextColor = OnSecondaryContainerDark,
            unselectedIconColor = OnSurfaceVariantMuted,
            unselectedTextColor = OnSurfaceVariantMuted
          )
        )

        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
              contentDescription = "Setelan"
            )
          },
          label = {
            Text(
              text = "Setelan",
              style = MaterialTheme.typography.labelSmall
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = SelectedPillLavender,
            selectedIconColor = OnSecondaryContainerDark,
            selectedTextColor = OnSecondaryContainerDark,
            unselectedIconColor = OnSurfaceVariantMuted,
            unselectedTextColor = OnSurfaceVariantMuted
          )
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter
    ) {
      when (selectedTab) {
        0 -> HomeScreenContent(
          currentProjectName = currentProjectName,
          uploadProgress = animatedProgress,
          uploadStatus = uploadStatus,
          isProcessing = isProcessing,
          onStartUploadClick = { showUploadDialog = true },
          onAnalyzeLogicClick = {
            startBuildPipeline("Logika_Analisis.kt")
          },
          onCompileApkClick = {
            startBuildPipeline("App_Release.apk")
          },
          onCardClick = { showLogViewerDialog = true }
        )
        1 -> HistoryScreenContent(
          historyList = historyList,
          onItemClick = { item ->
            Toast.makeText(context, "Memuat kembali ${item.projectName}", Toast.LENGTH_SHORT).show()
          }
        )
        2 -> SettingsScreenContent()
      }
    }
  }

  // Upload Choice Dialog
  if (showUploadDialog) {
    AlertDialog(
      onDismissRequest = { showUploadDialog = false },
      containerColor = SecondaryContainerLight,
      title = {
        Text(
          text = "Pilih Sumber Kode",
          style = MaterialTheme.typography.titleLarge,
          color = OnBackgroundDark
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Unggah file arsip zip atau tempelkan source code langsung untuk dikompilasi.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariantMuted
          )

          Card(
            onClick = {
              showUploadDialog = false
              startBuildPipeline("Project_Android_M3.zip")
            },
            colors = CardDefaults.cardColors(containerColor = PrimaryContainerLavender),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                tint = PrimaryPurple
              )
              Column {
                Text(
                  text = "Pilih File ZIP Proyek",
                  style = MaterialTheme.typography.titleMedium,
                  color = OnPrimaryContainerPurple
                )
                Text(
                  text = "Contoh: Project_Android_M3.zip",
                  style = MaterialTheme.typography.labelSmall,
                  color = OnPrimaryContainerPurple.copy(alpha = 0.8f)
                )
              }
            }
          }

          Card(
            onClick = {
              showUploadDialog = false
              showSourceCodeDialog = true
            },
            colors = CardDefaults.cardColors(containerColor = BackgroundLight),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = PrimaryPurple
              )
              Column {
                Text(
                  text = "Tempel Source Code (Kotlin / Jetpack)",
                  style = MaterialTheme.typography.titleMedium,
                  color = OnBackgroundDark
                )
                Text(
                  text = "Edit langsung kode Compose",
                  style = MaterialTheme.typography.labelSmall,
                  color = OnSurfaceVariantMuted
                )
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showUploadDialog = false }) {
          Text("Tutup", color = PrimaryPurple)
        }
      }
    )
  }

  // Source Code Paste Dialog
  if (showSourceCodeDialog) {
    AlertDialog(
      onDismissRequest = { showSourceCodeDialog = false },
      containerColor = SecondaryContainerLight,
      title = {
        Text(
          text = "Editor Source Code",
          style = MaterialTheme.typography.titleLarge,
          color = OnBackgroundDark
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Ketik atau paste kode Kotlin Jetpack Compose:",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariantMuted
          )

          OutlinedTextField(
            value = customCodeInput,
            onValueChange = { customCodeInput = it },
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp)
              .testTag("source_code_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = BackgroundLight,
              unfocusedContainerColor = BackgroundLight,
              focusedBorderColor = PrimaryPurple,
              unfocusedBorderColor = OutlineColor
            ),
            shape = RoundedCornerShape(12.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showSourceCodeDialog = false
            startBuildPipeline("Custom_Snippet.kt")
          },
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
          Text("Kompilasi Sekarang", color = OnPrimaryPurple)
        }
      },
      dismissButton = {
        TextButton(onClick = { showSourceCodeDialog = false }) {
          Text("Batal", color = OnSurfaceVariantMuted)
        }
      }
    )
  }

  // Log Viewer Dialog
  if (showLogViewerDialog) {
    AlertDialog(
      onDismissRequest = { showLogViewerDialog = false },
      containerColor = SecondaryContainerLight,
      title = {
        Text(
          text = "Build Output & Logs",
          style = MaterialTheme.typography.titleLarge,
          color = OnBackgroundDark
        )
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(BackgroundLight, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          buildLogs.forEach { log ->
            Text(
              text = "> $log",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 12.sp
              ),
              color = PrimaryPurple
            )
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showLogViewerDialog = false }) {
          Text("Tutup", color = PrimaryPurple)
        }
      }
    )
  }
}

@Composable
fun HomeScreenContent(
  currentProjectName: String,
  uploadProgress: Float,
  uploadStatus: String,
  isProcessing: Boolean,
  onStartUploadClick: () -> Unit,
  onAnalyzeLogicClick: () -> Unit,
  onCompileApkClick: () -> Unit,
  onCardClick: () -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .widthIn(max = 600.dp)
      .verticalScroll(scrollState)
      .padding(horizontal = 24.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    // Header Section with Bold Typography
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = "READY TO BUILD",
        style = MaterialTheme.typography.labelMedium,
        color = PrimaryPurple,
        modifier = Modifier.testTag("eyebrow_text")
      )

      Text(
        text = "Kirim Source\nCode Anda",
        style = MaterialTheme.typography.displayLarge,
        color = OnBackgroundDark,
        modifier = Modifier.testTag("hero_heading")
      )

      Text(
        text = "Saya akan menganalisis kode Anda dan menyusunnya menjadi aplikasi Android yang siap pakai.",
        style = MaterialTheme.typography.bodyLarge,
        color = OnSurfaceVariantMuted,
        modifier = Modifier
          .widthIn(max = 300.dp)
          .testTag("hero_description")
      )
    }

    // Main Upload & Status Card (#EADDFF with 28dp radius)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(
          elevation = 2.dp,
          shape = RoundedCornerShape(28.dp),
          spotColor = PrimaryPurple.copy(alpha = 0.15f)
        )
        .clickable { onCardClick() }
        .testTag("upload_status_card"),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = PrimaryContainerLavender)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .background(PrimaryPurple, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CloudUpload,
              contentDescription = "Cloud Upload Icon",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }

          Column {
            Text(
              text = currentProjectName,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
              color = OnPrimaryContainerPurple
            )
            Text(
              text = uploadStatus,
              style = MaterialTheme.typography.bodyMedium,
              color = OnPrimaryContainerPurple.copy(alpha = 0.75f)
            )
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Progress",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
              color = OnPrimaryContainerPurple
            )
            Text(
              text = "${(uploadProgress * 100).toInt()}%",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
              color = OnPrimaryContainerPurple
            )
          }

          LinearProgressIndicator(
            progress = { uploadProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(CircleShape),
            color = PrimaryPurple,
            trackColor = ProgressTrackColor
          )
        }
      }
    }

    // Grid of Action Tiles (Analisis Logika & Compile APK)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      FeatureTile(
        title = "Analisis Logika",
        icon = Icons.Default.Code,
        modifier = Modifier.weight(1f),
        onClick = onAnalyzeLogicClick
      )
      FeatureTile(
        title = "Compile APK",
        icon = Icons.Default.Build,
        modifier = Modifier.weight(1f),
        onClick = onCompileApkClick
      )
    }

    // Secondary Grid of Tiles
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      FeatureTile(
        title = "Validasi Layout",
        icon = Icons.Default.Dashboard,
        modifier = Modifier.weight(1f),
        onClick = onAnalyzeLogicClick
      )
      FeatureTile(
        title = "Simulasi Device",
        icon = Icons.Default.Smartphone,
        modifier = Modifier.weight(1f),
        onClick = onCompileApkClick
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Primary CTA Pill Button (#6750A4)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      contentAlignment = Alignment.Center
    ) {
      Button(
        onClick = onStartUploadClick,
        modifier = Modifier
          .shadow(
            elevation = 6.dp,
            shape = CircleShape,
            spotColor = PrimaryPurple.copy(alpha = 0.4f)
          )
          .testTag("start_upload_button"),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = PrimaryPurple,
          contentColor = OnPrimaryPurple
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
          horizontal = 36.dp,
          vertical = 16.dp
        )
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(
            imageVector = if (isProcessing) Icons.Default.PlayArrow else Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
          )
          Text(
            text = if (isProcessing) "Sedang Memproses..." else "Mulai Upload",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
          )
        }
      }
    }
  }
}

@Composable
fun FeatureTile(
  title: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(24.dp))
      .border(1.dp, OutlineColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
      .clickable { onClick() }
      .testTag("feature_tile_${title.replace(' ', '_').lowercase()}"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = SecondaryContainerLight)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = PrimaryPurple,
        modifier = Modifier.size(24.dp)
      )
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium
        ),
        color = OnBackgroundDark
      )
    }
  }
}

@Composable
fun HistoryScreenContent(
  historyList: List<BuildHistoryItem>,
  onItemClick: (BuildHistoryItem) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .widthIn(max = 600.dp)
      .padding(horizontal = 24.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "RIWAYAT PROYEK",
      style = MaterialTheme.typography.labelMedium,
      color = PrimaryPurple
    )
    Text(
      text = "Daftar Kompilasi",
      style = MaterialTheme.typography.headlineLarge,
      color = OnBackgroundDark
    )

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(historyList, key = { it.id }) { item ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) },
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = SecondaryContainerLight)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .background(SelectedPillLavender, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(24.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.projectName,
                style = MaterialTheme.typography.titleMedium,
                color = OnBackgroundDark
              )
              Text(
                text = "${item.timestamp} • ${item.linesOfCode} baris kode",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariantMuted
              )
            }

            Text(
              text = item.status,
              style = MaterialTheme.typography.labelSmall,
              color = PrimaryPurple
            )
          }
        }
      }
    }
  }
}

@Composable
fun SettingsScreenContent() {
  val context = LocalContext.current
  Column(
    modifier = Modifier
      .fillMaxSize()
      .widthIn(max = 600.dp)
      .padding(horizontal = 24.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    Text(
      text = "KONFIGURASI",
      style = MaterialTheme.typography.labelMedium,
      color = PrimaryPurple
    )
    Text(
      text = "Setelan Aplikasi",
      style = MaterialTheme.typography.headlineLarge,
      color = OnBackgroundDark
    )

    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = SecondaryContainerLight),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "Parameter Kompiler",
          style = MaterialTheme.typography.titleMedium,
          color = OnBackgroundDark
        )

        SettingRow(
          title = "Target SDK",
          value = "Android 15 (API 35/36)",
          onClick = { Toast.makeText(context, "SDK 36 aktif", Toast.LENGTH_SHORT).show() }
        )
        SettingRow(
          title = "Mode UI Compose",
          value = "Material 3 Bold Typography",
          onClick = { Toast.makeText(context, "Tema Bold Typography aktif", Toast.LENGTH_SHORT).show() }
        )
        SettingRow(
          title = "Optimasi R8/ProGuard",
          value = "Aktif (Release Ready)",
          onClick = { Toast.makeText(context, "Optimasi aktif", Toast.LENGTH_SHORT).show() }
        )
      }
    }
  }
}

@Composable
fun SettingRow(
  title: String,
  value: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
      color = OnBackgroundDark
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = PrimaryPurple
    )
  }
}

@Preview(showBackground = true)
@Composable
fun AiArchitectPreview() {
  MyApplicationTheme {
    AiArchitectApp()
  }
}

