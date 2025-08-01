# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Manter a classe PomodoroTimer e seu construtor vazio para desserialização do Firestore
-keepclassmembers class com.example.pomodo.model.PomodoroTimer {
    public <init>();
}

# Também mantém os campos públicos e privados usados na serialização (opcional)
-keepclassmembers class com.example.pomodo.model.PomodoroTimer {
    *;
}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
