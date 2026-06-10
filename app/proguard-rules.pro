# Android specific proguard rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Glide specific rules
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.load.data.** { *; }

# Keep all activities and their public methods
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Service

# Keep all view classes
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
