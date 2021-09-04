-allowaccessmodification

-overloadaggressively

-repackageclasses

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    static void checkNotNullParameter(java.lang.Object, java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# Allow enums to be deserialized when read from a Bundle
-keepclassmembers enum * {
    public static **[] values();
}

# https://issuetracker.google.com/issues/155947700
-assumenosideeffects public class kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    public java.lang.StackTraceElement getStackTraceElement() return null;
}
-checkdiscard class kotlin.coroutines.jvm.internal.DebugMetadata
