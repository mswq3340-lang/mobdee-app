// ============================================================================
// MOBDE NAFES ASSISTANT - ALL-IN-ONE SINGLE FILE IMPLEMENTATION
// Package: com.example.ui.screens
// Target: Android / Jetpack Compose / Material 3
// ============================================================================

package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

// ============================================================================
// 1. DATA MODELS & ENUMS
// ============================================================================

enum class AnswerSheetType {
    BUBBLE_SHEET, NORMAL_SHEET
}

enum class GradeLevel(val shortName: String) {
    GRADE_4("الرابع الابتدائي"),
    GRADE_5("الخامس الابتدائي"),
    GRADE_6("السادس الابتدائي")
}

enum class Semester(val label: String) {
    FIRST("الفصل الدراسي الأول"),
    SECOND("الفصل الدراسي الثاني"),
    THIRD("الفصل الدراسي الثالث")
}

data class Question(
    val questionNumber: Int,
    val text: String,
    val options: List<String>,
    val correctAnswer: String
)

data class Exam(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val grade: GradeLevel,
    val semester: Semester,
    val questions: List<Question>
)

data class StudentGrade(
    val id: String,
    val examId: String,
    val examTitle: String,
    val studentName: String,
    val seatNumber: String,
    val score: Double,
    val maxScore: Double,
    val percentage: Double,
    val correctCount: Int,
    val totalQuestions: Int,
    val studentAnswers: Map<Int, String>
)

data class MetricStats(
    val totalStudents: Int,
    val averageScore: Double,
    val averagePercentage: Double,
    val highestScore: Double,
    val passingCount: Int,
    val passingPercentage: Double,
    val excellentCount: Int,
    val veryGoodCount: Int,
    val goodCount: Int,
    val needsSupportCount: Int
)

// ============================================================================
// 2. THEME & COLORS
// ============================================================================

val NafesTealDark = Color(0xFF0F766E)
val NafesTealPrimary = Color(0xFF14B8A6)
val NafesTealMedium = Color(0xFF2DD4BF)
val NafesTealLight = Color(0xFFCCFBF1)
val NafesGreen = Color(0xFF10B981)
val NafesPurple = Color(0xFF8B5CF6)
val NafesOrange = Color(0xFFF59E0B)
val BackgroundLight = Color(0xFFF8FAFC)

// ============================================================================
// 3. REPOSITORY & STATE MANAGEMENT
// ============================================================================

class MobdeRepository {
    private val _savedExams = MutableStateFlow<List<Exam>>(
        listOf(
            Exam(
                id = "exam_nafes_1",
                title = "الاختبار الوطني التجريبي - مادة العلوم (نافس)",
                grade = GradeLevel.GRADE_6,
                semester = Semester.FIRST,
                questions = listOf(
                    Question(1, "ما هي الوحدة الأساسية بناء الكائنات الحية؟", listOf("أ) الخلية", "ب) النسيج", "ج) العضو", "د) الجهاز"), "أ"),
                    Question(2, "أي مما يلي يُعد من غازات الغلاف الجوي بنسبة أكبر؟", listOf("أ) الأكسجين", "ب) النيتروجين", "ج) ثاني أكسيد الكربون", "د) الهيدروجين"), "ب"),
                    Question(3, "قوة تبطئ حركة الأجسام عند احتكاكها بالسطوح:", listOf("أ) الجاذبية", "ب) المغناطيسية", "ج) الاحتكاك", "د) الدفع"), "ج")
                )
            )
        )
    )
    val savedExams: StateFlow<List<Exam>> = _savedExams.asStateFlow()

    private val _currentExam = MutableStateFlow<Exam?>(_savedExams.value.firstOrNull())
    val currentExam: StateFlow<Exam?> = _currentExam.asStateFlow()

    private val _studentGrades = MutableStateFlow<List<StudentGrade>>(
        listOf(
            StudentGrade(
                id = "grade_1",
                examId = "exam_nafes_1",
                examTitle = "الاختبار الوطني التجريبي - مادة العلوم (نافس)",
                studentName = "عبدالرحمن فهد السبيعي",
                seatNumber = "2045",
                score = 3.0,
                maxScore = 3.0,
                percentage = 100.0,
                correctCount = 3,
                totalQuestions = 3,
                studentAnswers = mapOf(1 to "أ", 2 to "ب", 3 to "ج")
            ),
            StudentGrade(
                id = "grade_2",
                examId = "exam_nafes_1",
                examTitle = "الاختبار الوطني التجريبي - مادة العلوم (نافس)",
                studentName = "زياد صالح العمري",
                seatNumber = "2048",
                score = 2.0,
                maxScore = 3.0,
                percentage = 66.6,
                correctCount = 2,
                totalQuestions = 3,
                studentAnswers = mapOf(1 to "أ", 2 to "أ", 3 to "ج")
            )
        )
    )
    val studentGrades: StateFlow<List<StudentGrade>> = _studentGrades.asStateFlow()

    private val modelAnswersMap = mutableMapOf<String, MutableMap<Int, String>>()

    fun getModelAnswerKey(examId: String): Map<Int, String> {
        return modelAnswersMap[examId] ?: emptyMap()
    }

    fun updateModelAnswerKey(examId: String, keyMap: Map<Int, String>) {
        modelAnswersMap[examId] = keyMap.toMutableMap()
    }

    fun addStudentGrade(grade: StudentGrade) {
        _studentGrades.value = listOf(grade) + _studentGrades.value
    }

    fun deleteStudentGrade(gradeId: String) {
        _studentGrades.value = _studentGrades.value.filter { it.id != gradeId }
    }

    fun deleteExam(examId: String) {
        _savedExams.value = _savedExams.value.filter { it.id != examId }
        if (_currentExam.value?.id == examId) {
            _currentExam.value = _savedExams.value.firstOrNull()
        }
    }

    fun calculateStatistics(): MetricStats {
        val grades = _studentGrades.value
        val total = grades.size
        if (total == 0) return MetricStats(0, 0.0, 0.0, 0.0, 0, 0.0, 0, 0, 0, 0)

        val avgScore = grades.map { it.score }.average()
        val avgPercentage = grades.map { it.percentage }.average()
        val maxScore = grades.maxOfOrNull { it.score } ?: 0.0
        val passing = grades.count { it.percentage >= 50.0 }
        val passingRate = (passing.toDouble() / total) * 100.0

        val excellent = grades.count { it.percentage >= 90.0 }
        val veryGood = grades.count { it.percentage in 80.0..89.9 }
        val good = grades.count { it.percentage in 65.0..79.9 }
        val needsSupport = grades.count { it.percentage < 65.0 }

        return MetricStats(
            totalStudents = total,
            averageScore = avgScore,
            averagePercentage = avgPercentage,
            highestScore = maxScore,
            passingCount = passing,
            passingPercentage = passingRate,
            excellentCount = excellent,
            veryGoodCount = veryGood,
            goodCount = good,
            needsSupportCount = needsSupport
        )
    }
}

// ============================================================================
// 4. MOCK EXPORT & UI HELPERS
// ============================================================================

object DocumentExportHelper {
    fun printExamToPdf(context: Context, exam: Exam, isModelAnswer: Boolean, answerKey: Map<Int, String>? = null) {
        Toast.makeText(context, "جاري طباعة نموذج '${exam.title}' بصيغة PDF...", Toast.LENGTH_SHORT).show()
    }

    fun exportExamToWord(context: Context, exam: Exam, isModelAnswer: Boolean, answerKey: Map<Int, String>? = null) {
        Toast.makeText(context, "جاري تصدير اختبار '${exam.title}' إلى مستند Word...", Toast.LENGTH_SHORT).show()
    }

    fun printAnalyticsToPdf(context: Context, title: String, grades: List<StudentGrade>, stats: MetricStats) {
        Toast.makeText(context, "جاري تصدير تقرير التحليلات والدرجات PDF...", Toast.LENGTH_SHORT).show()
    }

    fun exportGradesToExcel(context: Context, title: String, grades: List<StudentGrade>, stats: MetricStats) {
        Toast.makeText(context, "جاري تصدير كشوفات الدرجات إلى Excel...", Toast.LENGTH_SHORT).show()
    }

    fun exportAnalyticsToWord(context: Context, title: String, grades: List<StudentGrade>, stats: MetricStats) {
        Toast.makeText(context, "جاري تصدير تقرير التحليلات إلى Word...", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun NafesLogoCompact() {
    Surface(
        color = NafesTealLight,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, NafesTealPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.School, contentDescription = null, tint = NafesTealDark, modifier = Modifier.size(14.dp))
            Text("منظومة مِسبار", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NafesTealDark)
        }
    }
}

@Composable
fun NafesExamHeader(exam: Exam, isModelAnswer: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, NafesTealPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NafesTealDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text("الصف: ${exam.grade.shortName} • ${exam.semester.label}", fontSize = 11.sp, color = Color.Gray)
            if (isModelAnswer) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("نموذج إجابة معتمد للمصحح الآلي", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NafesGreen)
            }
        }
    }
}

// ============================================================================
// 5. SCREENS IMPLEMENTATION (AnswerSheetScreen, ScannerScreen, LibraryScreen, AnalyticsScreen)
// ============================================================================

@Composable
fun AnswerSheetScreen(
    repository: MobdeRepository,
    onNavigateToScanner: () -> Unit
) {
    val context = LocalContext.current
    val currentExam by repository.currentExam.collectAsState()
    val savedExams by repository.savedExams.collectAsState()

    var selectedSheetType by remember { mutableStateOf(AnswerSheetType.BUBBLE_SHEET) }
    var isModelAnswerSolved by remember { mutableStateOf(false) }
    var activeExam by remember { mutableStateOf(currentExam ?: savedExams.firstOrNull()) }

    val currentKeyMap = remember(activeExam?.id) {
        val initialMap = mutableStateMapOf<Int, String>()
        activeExam?.let { ex ->
            val existing = repository.getModelAnswerKey(ex.id)
            if (existing.isNotEmpty()) {
                initialMap.putAll(existing)
            } else {
                ex.questions.forEach { q -> initialMap[q.questionNumber] = q.correctAnswer }
            }
        }
        initialMap
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, NafesTealPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("إنشاء نموذج الإجابة وأوراق التظليل", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NafesTealDark)
                            Text("تظليل القدرات ونافس أو نموذج عادي مع خيار الحل للتصحيح بالكاميرا", fontSize = 11.sp, color = Color.Gray)
                        }
                        NafesLogoCompact()
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("١. نمط ورقة الإجابة:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NafesTealDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AnswerSheetType.values().forEach { type ->
                            val isSelected = selectedSheetType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) NafesTealPrimary else Color(0xFFF1F5F9))
                                    .clickable { selectedSheetType = type }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (type == AnswerSheetType.BUBBLE_SHEET) "تظليل مثل القدرات / نافس" else "نموذج عادي",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("٢. نوع النموذج المطلوب:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NafesTealDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isModelAnswerSolved) NafesPurple else Color(0xFFF1F5F9))
                                .clickable { isModelAnswerSolved = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ورقة الطالب (فارغة للتظليل)", fontSize = 12.sp, fontWeight = if (!isModelAnswerSolved) FontWeight.Bold else FontWeight.Normal, color = if (!isModelAnswerSolved) Color.White else Color.DarkGray)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isModelAnswerSolved) NafesGreen else Color(0xFFF1F5F9))
                                .clickable { isModelAnswerSolved = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("نموذج إجابة محلول (للمعلم)", fontSize = 12.sp, fontWeight = if (isModelAnswerSolved) FontWeight.Bold else FontWeight.Normal, color = if (isModelAnswerSolved) Color.White else Color.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                activeExam?.let { ex -> DocumentExportHelper.printExamToPdf(context, ex, isModelAnswerSolved, currentKeyMap) }
                                    ?: Toast.makeText(context, "الرجاء توليد اختبار أولاً", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NafesTealPrimary)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة PDF", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                activeExam?.let { ex -> DocumentExportHelper.exportExamToWord(context, ex, isModelAnswerSolved, currentKeyMap) }
                                    ?: Toast.makeText(context, "الرجاء توليد اختبار أولاً", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تصدير Word", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                activeExam?.let { ex ->
                                    repository.updateModelAnswerKey(ex.id, currentKeyMap)
                                    Toast.makeText(context, "تم حفظ نموذج الإجابة في المحفوظات بنجاح!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NafesGreen)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حفظ المفتاح", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onNavigateToScanner,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NafesTealDark)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الانتقال إلى تصحيح أوراق الطلاب بالكاميرا فوراً", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        activeExam?.let { exam ->
            item { NafesExamHeader(exam = exam, isModelAnswer = isModelAnswerSolved) }
            item {
                BubbleSheetPreview(
                    totalQuestions = exam.questions.size,
                    isSolved = isModelAnswerSolved,
                    modelAnswers = currentKeyMap,
                    onAnswerChange = { qNum, selectedOption ->
                        currentKeyMap[qNum] = selectedOption
                        repository.updateModelAnswerKey(exam.id, currentKeyMap)
                    }
                )
            }
        }
    }
}

@Composable
fun BubbleSheetPreview(
    totalQuestions: Int,
    isSolved: Boolean,
    modelAnswers: Map<Int, String>,
    onAnswerChange: (Int, String) -> Unit
) {
    val options = listOf("أ", "ب", "ج", "د")

    Card(
        modifier = Modifier.fillMaxWidth().border(1.5.dp, NafesTealPrimary, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (isSolved) "مفتاح الإجابة النموذجي (معتمد للمصحح الآلي)" else "تعليمات التظليل بقلم الرصاص أو الحبر الأسود",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isSolved) NafesGreen else NafesTealDark
                    )
                    Text(
                        if (isSolved) "اضغط على أي دائرة لتعديل الإجابة النموذجية المعتمدة" else "تظليل الدائرة كاملاً دون ثني الورقة",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                Icon(Icons.Default.QrCode, contentDescription = "Barcode", modifier = Modifier.size(32.dp), tint = NafesTealDark)
            }

            Spacer(modifier = Modifier.height(14.dp))

            val count = totalQuestions.coerceAtLeast(1)
            for (i in 1..count) {
                val selectedAnswer = modelAnswers[i] ?: ""
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$i", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.width(36.dp))
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                        options.forEach { opt ->
                            val isBubbleSelected = isSolved && (selectedAnswer.startsWith(opt) || selectedAnswer == opt)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isBubbleSelected) NafesGreen else Color.White)
                                    .border(if (isBubbleSelected) 2.dp else 1.5.dp, if (isBubbleSelected) NafesGreen else Color(0xFF64748B), CircleShape)
                                    .clickable(enabled = isSolved) { onAnswerChange(i, opt) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(opt, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isBubbleSelected) Color.White else Color(0xFF64748B))
                            }
                        }
                    }
                }
                if (i < count) Divider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun ScannerScreen(
    repository: MobdeRepository,
    onNavigateToLibrary: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentExam by repository.currentExam.collectAsState()
    val savedExams by repository.savedExams.collectAsState()
    val activeExam = currentExam ?: savedExams.firstOrNull()

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    var scannedGradeResult by remember { mutableStateOf<StudentGrade?>(null) }
    var showGradeDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "إذن الكاميرا مطلوب للمسح الضوئي لأوراق الإجابة", Toast.LENGTH_LONG).show()
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            simulateGradingResult(activeExam, repository) { grade ->
                scannedGradeResult = grade
                showGradeDialog = true
            }
        }
    }

    val mockStudentNames = listOf(
        "عبدالرحمن فهد السبيعي", "زياد صالح العمري", "محمد إبراهيم الزهراني",
        "خالد ناصر المطيري", "ياسر عبدالعزيز العتيبي", "أنس فيصل المالكي"
    )
    var studentNameCounter by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
                        } catch (e: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("مصحح نماذج الاختبارات الذكي", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = NafesTealPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("منح إذن الكاميرا والتصحيح")
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .border(2.5.dp, Brush.linearGradient(listOf(NafesGreen, NafesTealPrimary, NafesPurple)), RoundedCornerShape(20.dp))
            ) {
                Box(modifier = Modifier.align(Alignment.Center).fillMaxWidth().height(2.dp).background(NafesGreen.copy(alpha = 0.8f)))
            }
        }

        Surface(
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ضع ورقة تظليل الطالب داخل الإطار ثم اضغط زر التصحيح", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { galleryPickerLauncher.launch("image/*") },
                        modifier = Modifier.size(46.dp).background(Color.DarkGray, CircleShape)
                    ) {
                        Icon(Icons.Default.Collections, contentDescription = "Gallery", tint = Color.White)
                    }

                    Button(
                        onClick = {
                            if (activeExam == null) {
                                Toast.makeText(context, "الرجاء إنشاء وحفظ اختبار أولاً", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val name = mockStudentNames[studentNameCounter % mockStudentNames.size]
                            studentNameCounter++
                            simulateGradingResult(activeExam, repository, name) { grade ->
                                scannedGradeResult = grade
                                showGradeDialog = true
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = NafesGreen),
                        modifier = Modifier.size(68.dp).testTag("scan_sheet_button")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    IconButton(
                        onClick = onNavigateToLibrary,
                        modifier = Modifier.size(46.dp).background(NafesTealPrimary, CircleShape)
                    ) {
                        Icon(Icons.Default.LocalLibrary, contentDescription = "Library", tint = Color.White)
                    }
                }
            }
        }
    }

    if (showGradeDialog && scannedGradeResult != null) {
        val grade = scannedGradeResult!!
        var editedStudentName by remember { mutableStateOf(grade.studentName) }
        var editedSeatNumber by remember { mutableStateOf(grade.seatNumber) }

        AlertDialog(
            onDismissRequest = { showGradeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NafesGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تم تصحيح الورقة بنجاح!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NafesTealDark)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = editedStudentName, onValueChange = { editedStudentName = it }, label = { Text("اسم الطالب") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editedSeatNumber, onValueChange = { editedSeatNumber = it }, label = { Text("رقم الجلوس") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                    Surface(color = NafesTealLight, shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("الدرجة", fontSize = 11.sp, color = Color.Gray)
                                Text("${grade.score} / ${grade.maxScore}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NafesTealDark)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("النسبة", fontSize = 11.sp, color = Color.Gray)
                                Text("${String.format(Locale.US, "%.1f", grade.percentage)}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NafesGreen)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalGrade = grade.copy(studentName = editedStudentName, seatNumber = editedSeatNumber)
                        repository.addStudentGrade(finalGrade)
                        Toast.makeText(context, "تم حفظ نتيجة ${finalGrade.studentName} في المكتبة والمحفوظات!", Toast.LENGTH_SHORT).show()
                        showGradeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NafesTealPrimary)
                ) {
                    Text("حفظ في المكتبة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGradeDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

private fun simulateGradingResult(
    exam: Exam?,
    repository: MobdeRepository,
    studentName: String = "عبدالرحمن فهد السبيعي",
    onResult: (StudentGrade) -> Unit
) {
    if (exam == null) return
    val total = exam.questions.size
    val key = repository.getModelAnswerKey(exam.id)
    val correctCount = (total * 0.9).toInt().coerceAtMost(total)
    val score = correctCount.toDouble()
    val maxScore = total.toDouble()
    val percentage = (score / maxScore) * 100.0

    val studentAnswers = mutableMapOf<Int, String>()
    exam.questions.forEachIndexed { index, q ->
        val correctAns = key[q.questionNumber] ?: q.correctAnswer
        studentAnswers[q.questionNumber] = if (index < correctCount) correctAns else if (correctAns == "أ") "ب" else "أ"
    }

    onResult(
        StudentGrade(
            id = UUID.randomUUID().toString(),
            examId = exam.id,
            examTitle = exam.title,
            studentName = studentName,
            seatNumber = "20${(10..99).random()}",
            score = score,
            maxScore = maxScore,
            percentage = percentage,
            correctCount = correctCount,
            totalQuestions = total,
            studentAnswers = studentAnswers
        )
    )
}

@Composable
fun LibraryScreen(
    repository: MobdeRepository,
    onSelectExamForAnswerSheet: (Exam) -> Unit
) {
    val context = LocalContext.current
    val savedExams by repository.savedExams.collectAsState()
    val studentGrades by repository.studentGrades.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("سجل درجات الطلاب", "المحفوظات (الأسئلة ونماذج الإجابة)")
    val stats = remember(studentGrades) { repository.calculateStatistics() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, NafesTealPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("مكتبة المعلم والمحفوظات - نافس", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NafesTealDark)
                            Text("إدارة نتائج التصحيح، كشوفات الدرجات، والنماذج المحفوظة", fontSize = 11.sp, color = Color.Gray)
                        }
                        NafesLogoCompact()
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color(0xFFF1F5F9), modifier = Modifier.clip(RoundedCornerShape(10.dp))) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp, color = if (selectedTabIndex == index) NafesTealPrimary else Color.DarkGray) }
                            )
                        }
                    }
                }
            }
        }

        if (selectedTabIndex == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.5.dp, NafesTealPrimary, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("المؤشرات الإحصائية العامة للطلاب:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NafesTealDark)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricTile("المجموع الكلي", "${stats.totalStudents}", "طالب مختبر", NafesTealDark, Modifier.weight(1f))
                            MetricTile("المتوسط الحسابي", String.format(Locale.US, "%.1f", stats.averageScore), "${String.format(Locale.US, "%.1f", stats.averagePercentage)}%", NafesGreen, Modifier.weight(1f))
                            MetricTile("أعلى درجة", "${stats.highestScore}", "الدرجة القصوى", NafesPurple, Modifier.weight(1f))
                            MetricTile("نسبة الاجتياز", "${String.format(Locale.US, "%.0f", stats.passingPercentage)}%", "${stats.passingCount} مجتازين", NafesTealPrimary, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (studentGrades.isEmpty()) Toast.makeText(context, "لا توجد درجات للتصدير", Toast.LENGTH_SHORT).show()
                                    else DocumentExportHelper.printAnalyticsToPdf(context, "كشف درجات الاختبار الوطني نافس", studentGrades, stats)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NafesTealPrimary)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("طباعة PDF", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    if (studentGrades.isEmpty()) Toast.makeText(context, "لا توجد درجات للتصدير", Toast.LENGTH_SHORT).show()
                                    else DocumentExportHelper.exportGradesToExcel(context, "كشف درجات طلاب نافس", studentGrades, stats)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NafesGreen)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تصدير إكسل Excel", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            items(studentGrades) { grade ->
                StudentGradeCard(grade = grade, onDelete = { repository.deleteStudentGrade(grade.id) })
            }
        }

        if (selectedTabIndex == 1) {
            items(savedExams) { exam ->
                SavedExamCard(
                    exam = exam,
                    onSelect = { onSelectExamForAnswerSheet(exam) },
                    onDelete = { repository.deleteExam(exam.id) },
                    onPrintPdf = { DocumentExportHelper.printExamToPdf(context, exam, isModelAnswer = false) },
                    onExportWord = { DocumentExportHelper.exportExamToWord(context, exam, isModelAnswer = false) }
                )
            }
        }
    }
}

@Composable
fun MetricTile(label: String, value: String, subtext: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = color.copy(alpha = 0.08f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 10.sp, color = Color.DarkGray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(subtext, fontSize = 9.sp, color = Color.Gray)
        }
    }
}

@Composable
fun StudentGradeCard(grade: StudentGrade, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(38.dp).background(if (grade.percentage >= 90) NafesGreen.copy(alpha = 0.15f) else NafesTealLight, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${String.format(Locale.US, "%.0f", grade.percentage)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (grade.percentage >= 90) NafesGreen else NafesTealDark)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(grade.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NafesTealDark)
                    Text("رقم الجلوس: ${grade.seatNumber} • الدرجة: ${grade.score} من ${grade.maxScore}", fontSize = 11.sp, color = Color.Gray)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SavedExamCard(exam: Exam, onSelect: () -> Unit, onDelete: () -> Unit, onPrintPdf: () -> Unit, onExportWord: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NafesTealDark)
                    Text("${exam.grade.shortName} • ${exam.semester.label} • ${exam.questions.size} سؤال", fontSize = 11.sp, color = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = onSelect, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = NafesTealPrimary)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نموذج الإجابة والتصحيح", fontSize = 11.sp)
                }
                OutlinedButton(onClick = onPrintPdf, shape = RoundedCornerShape(8.dp)) { Text("PDF", fontSize = 11.sp) }
                OutlinedButton(onClick = onExportWord, shape = RoundedCornerShape(8.dp)) { Text("وورد", fontSize = 11.sp) }
            }
        }
    }
}

@Composable
fun AnalyticsScreen(repository: MobdeRepository) {
    val context = LocalContext.current
    val studentGrades by repository.studentGrades.collectAsState()
    val currentExam by repository.currentExam.collectAsState()
    val stats = remember(studentGrades) { repository.calculateStatistics() }
    val examTitle = currentExam?.title ?: "الاختبار الوطني التجريبي - نافس"

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, NafesTealPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("تحليل نتائج الطلاب ومؤشرات نافس", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NafesTealDark)
                            Text("تقارير تشخيصية تفصيلية ومستويات الإتقان والخطة العلاجية", fontSize = 11.sp, color = Color.Gray)
                        }
                        NafesLogoCompact()
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (studentGrades.isEmpty()) Toast.makeText(context, "لا توجد نتائج مصححة للتحليل حالياً", Toast.LENGTH_SHORT).show()
                                else DocumentExportHelper.printAnalyticsToPdf(context, examTitle, studentGrades, stats)
                            },
                            modifier = Modifier.weight(1f).testTag("analytics_print_pdf"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NafesTealPrimary)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة تقرير PDF", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                if (studentGrades.isEmpty()) Toast.makeText(context, "لا توجد نتائج مصححة للتحليل حالياً", Toast.LENGTH_SHORT).show()
                                else DocumentExportHelper.exportAnalyticsToWord(context, examTitle, studentGrades, stats)
                            },
                            modifier = Modifier.weight(1f).testTag("analytics_export_word"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تصدير Word", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("تصنيف الطلاب وفق مستويات الأداء المعيارية لنافس:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NafesTealDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    val total = if (stats.totalStudents > 0) stats.totalStudents.toFloat() else 1f
                    PerformanceLevelProgress("المستوى المتقدم (90% فأعلى)", stats.excellentCount, stats.excellentCount / total, NafesGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    PerformanceLevelProgress("المستوى المتقن (80% - 89%)", stats.veryGoodCount, stats.veryGoodCount / total, NafesTealMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    PerformanceLevelProgress("المستوى المتمكن (65% - 79%)", stats.goodCount, stats.goodCount / total, NafesOrange)
                    Spacer(modifier = Modifier.height(8.dp))
                    PerformanceLevelProgress("يحتاج إلى دعم وتدخل (أقل من 65%)", stats.needsSupportCount, stats.needsSupportCount / total, Color(0xFFE53935))
                }
            }
        }
    }
}

@Composable
fun PerformanceLevelProgress(label: String, count: Int, percentage: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
            Text("$count طلاب (${String.format(Locale.US, "%.0f", percentage * 100)}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
