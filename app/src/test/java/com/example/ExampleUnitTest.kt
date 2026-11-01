package com.example

import com.example.ui.components.HangulKeyboardUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun hangulKeyboardUtil_decomposesWordCorrectly() {
    // "한국" -> [ㅎ, ㅏ, ㄴ, ㄱ, ㅜ, ㄱ]
    val strokes = HangulKeyboardUtil.decompose("한국")
    assertEquals(listOf('ㅎ', 'ㅏ', 'ㄴ', 'ㄱ', 'ㅜ', 'ㄱ'), strokes)

    // "사랑" -> [ㅅ, ㅏ, ㄹ, ㅏ, ㅇ]
    val loveStrokes = HangulKeyboardUtil.decompose("사랑")
    assertEquals(listOf('ㅅ', 'ㅏ', 'ㄹ', 'ㅏ', 'ㅇ'), loveStrokes)

    // "김치" -> [ㄱ, ㅣ, ㅁ, ㅊ, ㅣ]
    val kimchiStrokes = HangulKeyboardUtil.decompose("김치")
    assertEquals(listOf('ㄱ', 'ㅣ', 'ㅁ', 'ㅊ', 'ㅣ'), kimchiStrokes)
  }

  @Test
  fun hangulKeyboardUtil_handClassificationIsAccurate() {
    // Left hand consonants (자음)
    assertTrue(HangulKeyboardUtil.isLeftHandConsonant('ㄱ'))
    assertTrue(HangulKeyboardUtil.isLeftHandConsonant('ㄴ'))
    assertTrue(HangulKeyboardUtil.isLeftHandConsonant('ㅁ'))
    assertTrue(HangulKeyboardUtil.isLeftHandConsonant('ㅂ'))
    assertTrue(HangulKeyboardUtil.isLeftHandConsonant('ㅎ'))

    // Right hand vowels (모음)
    assertFalse(HangulKeyboardUtil.isLeftHandConsonant('ㅏ'))
    assertFalse(HangulKeyboardUtil.isLeftHandConsonant('ㅓ'))
    assertFalse(HangulKeyboardUtil.isLeftHandConsonant('ㅗ'))
    assertFalse(HangulKeyboardUtil.isLeftHandConsonant('ㅜ'))
    assertFalse(HangulKeyboardUtil.isLeftHandConsonant('ㅣ'))
  }

  @Test
  fun hangulKeyboardUtil_qwertyKeyHintsMatchStandard2BeolSik() {
    assertEquals("R", HangulKeyboardUtil.getQwertyKeyHint('ㄱ'))
    assertEquals("S", HangulKeyboardUtil.getQwertyKeyHint('ㄴ'))
    assertEquals("Q", HangulKeyboardUtil.getQwertyKeyHint('ㅂ'))
    assertEquals("K", HangulKeyboardUtil.getQwertyKeyHint('ㅏ'))
    assertEquals("J", HangulKeyboardUtil.getQwertyKeyHint('ㅓ'))
    assertEquals("H", HangulKeyboardUtil.getQwertyKeyHint('ㅗ'))
    assertEquals("N", HangulKeyboardUtil.getQwertyKeyHint('ㅜ'))
    assertEquals("L", HangulKeyboardUtil.getQwertyKeyHint('ㅣ'))
    assertEquals("Shift+R", HangulKeyboardUtil.getQwertyKeyHint('ㄲ'))
    assertEquals("Shift+Q", HangulKeyboardUtil.getQwertyKeyHint('ㅃ'))
  }
}
