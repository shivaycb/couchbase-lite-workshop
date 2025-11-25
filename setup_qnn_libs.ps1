$QNN_SDK_PATH = "C:\Users\rawat\Downloads\v2.28.0.241029\qairt\2.28.0.241029"
$TARGET_DIR = "c:\Users\rawat\couchbase-lite-workshop\app\src\main\jniLibs\arm64-v8a"

# Create target directory
New-Item -ItemType Directory -Force -Path $TARGET_DIR

# List of QNN libraries to copy
$LIBS = @(
    "libQnnCommon.so",
    "libQnnHtp.so",
    "libQnnHtpPrepare.so",
    "libQnnSystem.so",
    "libQnnHtpV75Stub.so", # Assuming S25 Ultra/Snapdragon 8 Gen 3/4 uses v75 or newer. Copying stub.
    "libQnnHtpV73Stub.so",
    "libQnnHtpV69Stub.so"
)

# Copy libraries
foreach ($lib in $LIBS) {
    $src = "$QNN_SDK_PATH\lib\aarch64-android\$lib"
    if (Test-Path $src) {
        Copy-Item -Path $src -Destination $TARGET_DIR -Force
        Write-Host "Copied $lib"
    } else {
        Write-Host "Warning: $lib not found at $src"
    }
}

# Also copy libc++_shared.so if needed, but Gradle usually handles it.
