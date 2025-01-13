-optimizations !method/specialization/**

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Don't print notes about potential mistakes or omissions in the configuration for kotlinx-serialization classes
# See also https://github.com/Kotlin/kotlinx.serialization/issues/1900
-dontnote kotlinx.serialization.**

# Serialization core uses `java.lang.ClassValue` for caching inside these specified classes.
# If there is no `java.lang.ClassValue` (for example, in Android), then R8/ProGuard will print a warning.
# However, since in this case they will not be used, we can disable these warnings
-dontwarn kotlinx.serialization.internal.ClassValueReferences

# disable optimisation for descriptor field because in some versions of ProGuard, optimization generates incorrect bytecode that causes a verification error
# see https://github.com/Kotlin/kotlinx.serialization/issues/2719
-keepclassmembers public class **$$serializer {
    private ** descriptor;
}

# When editing this file, update the following files as well:
# - META-INF/com.android.tools/proguard/coroutines.pro
# - META-INF/com.android.tools/r8/coroutines.pro

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class * implements kotlinx.coroutines.internal.MainDispatcherFactory

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

# These classes are only required by kotlinx.coroutines.debug.internal.AgentPremain, which is only loaded when
# kotlinx-coroutines-core is used as a Java agent, so these are not needed in contexts where ProGuard is used.
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.Signal

# Only used in `kotlinx.coroutines.internal.ExceptionsConstructor`.
# The case when it is not available is hidden in a `try`-`catch`, as well as a check for Android.
-dontwarn java.lang.ClassValue

# An annotation used for build tooling, won't be directly accessed.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement