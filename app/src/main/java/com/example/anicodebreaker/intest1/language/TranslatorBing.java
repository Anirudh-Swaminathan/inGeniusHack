package com.example.anicodebreaker.intest1.language;

import android.util.Log;

import com.example.anicodebreaker.intest1.CaptureActivity;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class TranslatorBing {
  private static final String TAG = TranslatorBing.class.getSimpleName();
  private static final String CLIENT_ID = " [PUT YOUR CLIENT ID HERE] ";
  private static final String CLIENT_SECRET = " [PUT YOUR CLIENT SECRET HERE] ";
  static String translate(String sourceLanguageCode, String targetLanguageCode, String sourceText) {
    Translate.setClientId(CLIENT_ID);
    Translate.setClientSecret(CLIENT_SECRET);
    try {
      Log.d(TAG, sourceLanguageCode + " -> " + targetLanguageCode);
      return Translate.execute(sourceText, Language.fromString(sourceLanguageCode),
          Language.fromString(targetLanguageCode));
    } catch (Exception e) {
      e.printStackTrace();
      return Translator.BAD_TRANSLATION_MSG;
    }
  }
  public static String toLanguage(String languageName) throws IllegalArgumentException {    
    // Convert string to all caps
    String standardizedName = languageName.toUpperCase();
    
    // Replace spaces with underscores
    standardizedName = standardizedName.replace(' ', '_');
    
    // Remove parentheses
    standardizedName = standardizedName.replace("(", "");   
    standardizedName = standardizedName.replace(")", "");
    
    // Map Norwegian-Bokmal to Norwegian
    if (standardizedName.equals("NORWEGIAN_BOKMAL")) {
      standardizedName = "NORWEGIAN";
    }
    
    try {
      return Language.valueOf(standardizedName).toString();
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "Not found--returning default language code");
      return CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE;
    }
  }
}