-keep class org.cef.** { *; }
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory

# kotlinx.serialization — keep generated serializers and @Serializable members
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * { *; }

# Third-party / transitive libraries with optional (unresolved) references.
# These are reference-only warnings from R8/ProGuard; the classes are not used at
# runtime by the desktop app, so suppressing the warnings is safe and is required
# for the release distribution (proguardReleaseJars) to complete.
-dontwarn ai.koog.**
-dontwarn androidx.compose.**
-dontwarn autovalue.shaded.**
-dontwarn aws.smithy.**
-dontwarn ch.qos.**
-dontwarn co.yml.**
-dontwarn com.aallam.**
-dontwarn com.couchbase.**
-dontwarn com.google.**
-dontwarn com.jogamp.**
-dontwarn com.sun.javafx.**
-dontwarn javafx.**
-dontwarn community.flock.**
-dontwarn io.github.**
-dontwarn io.ktor.**
-dontwarn io.lettuce.**
-dontwarn io.netty.**
-dontwarn io.opentelemetry.**
-dontwarn kotbase.**
-dontwarn net.htmlparser.**
-dontwarn okhttp3.internal.**
-dontwarn org.apache.**
-dontwarn reactor.core.**
-dontwarn reactor.util.**
