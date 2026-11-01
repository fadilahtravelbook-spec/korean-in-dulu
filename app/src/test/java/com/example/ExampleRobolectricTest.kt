package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Korean In Dulu", appName)
  }

  @Test
  fun `hangul alphabet repository is loaded with core alphabet`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = com.example.data.repository.KoreanRepository(context)
    val alphabet = repository.hangulAlphabet
    assertEquals(30, alphabet.size)
  }

  @Test
  fun `speech evaluation accurately scores exact Korean match`() {
    val assessment = com.example.util.SpeechRecognitionHelper.evaluatePronunciation(
      targetKorean = "안녕하세요",
      spokenKorean = "안녕하세요"
    )
    assertEquals(100, assessment.accuracy)
    assertEquals("안녕하세요", assessment.recognizedText)
    assertEquals(5, assessment.syllableBreakdown.size)
  }

  @Test
  fun `speech evaluation handles partial recognition gracefully`() {
    val assessment = com.example.util.SpeechRecognitionHelper.evaluatePronunciation(
      targetKorean = "감사합니다",
      spokenKorean = "감사"
    )
    assert(assessment.accuracy > 0)
    assert(assessment.feedback.isNotEmpty())
  }

  @Test
  fun `template placeholders produce pure Korean and pure Indonesian text`() {
    val templateKo = "안녕하세요! 제 이름은 {{nama}}입니다. 저는 {{asal}} 사람입니다."
    val templateId = "Halo! Nama saya adalah {{nama}}. Saya orang {{asal}}."
    val namaVal = "Fadilah"
    val asalVal = "인도네시아 (Indonesia)"

    val koAsal = if (asalVal.contains(" (")) asalVal.substringBefore(" (").trim() else asalVal
    val idAsal = if (asalVal.contains(" (") && asalVal.endsWith(")")) asalVal.substringAfter(" (").removeSuffix(")").trim() else asalVal

    val resultKo = templateKo.replace("{{nama}}", namaVal).replace("{{asal}}", koAsal)
    val resultId = templateId.replace("{{nama}}", namaVal).replace("{{asal}}", idAsal)

    assertEquals("안녕하세요! 제 이름은 Fadilah입니다. 저는 인도네시아 사람입니다.", resultKo)
    assertEquals("Halo! Nama saya adalah Fadilah. Saya orang Indonesia.", resultId)
  }

  @Test
  fun `hangul keyboard compose accurately combines jamo to korean text`() {
    val jamosHangeul = listOf('ㅎ', 'ㅏ', 'ㄴ', 'ㄱ', 'ㅡ', 'ㄹ')
    val composed = com.example.ui.components.HangulKeyboardUtil.compose(jamosHangeul)
    assertEquals("한글", composed)

    val jamosAnnyeong = com.example.ui.components.HangulKeyboardUtil.decompose("안녕하세요")
    val recomposed = com.example.ui.components.HangulKeyboardUtil.compose(jamosAnnyeong)
    assertEquals("안녕하세요", recomposed)
  }

  @Test
  fun `speech evaluation on blank speech gives constructive fallback`() {
    val assessment = com.example.util.SpeechRecognitionHelper.evaluatePronunciation(
      targetKorean = "감사합니다",
      spokenKorean = ""
    )
    assertEquals("(Tidak terdeteksi)", assessment.recognizedText)
    assert(assessment.accuracy in 30..60)
  }

  @Test
  fun `tts cleaner sanitizes brackets slashes and romanization for natural speech`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val ttsManager = com.example.util.TtsManager(context)

    val cleanSlash = ttsManager.sanitizeKoreanForTts("하나 / 한")
    assertEquals("하나, 한", cleanSlash)

    val cleanRoman = ttsManager.sanitizeKoreanForTts("행복 [haeng-bok]")
    assertEquals("행복", cleanRoman)

    val cleanParen = ttsManager.sanitizeKoreanForTts("안녕하세요 (Halo)")
    assertEquals("안녕하세요", cleanParen)
  }

  @Test
  fun `repository contains expanded role play scenarios and listening quizzes`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.repository.KoreanRepository(context)

    assert(repo.rolePlaySteps.size >= 12)
    assert(repo.listeningQuizzes.size >= 15)
  }
}
