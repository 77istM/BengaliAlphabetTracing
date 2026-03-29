# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve source file names and line numbers so crash-report stack traces
# (e.g. from Google Play's Android Vitals or Firebase Crashlytics) remain
# human-readable after R8 minification.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep enum constants (used for LetterCategory)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}