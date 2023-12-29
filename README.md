# S3 Backup Utility Mock

This is a utility to back-up files to S3. A fake S3 client is used, 
which simply stores back-up files on 
local disk.

# Execution
By default, backup files are stored in folder `storage_dir`. 
This can be changed by setting `storageDir=<path-to-the-storage-dir>` 
in `gradle.properties`

## Example commands

Backup folder `src/main/kotlin` and all its files
```bash
./gradlew run --args="save src/main/kotlin"
```

Restore folder `src/main/kotlin` and all its files to the current folder
```bash
./gradlew run --args="restore src/main/kotlin ."
```

Restore single file from folder `src/main/kotlin` to the current folder
```bash
./gradlew run --args="restore src/main/kotlin . kotlin/Main.kt"
./gradlew run --args="restore src/main/kotlin . kotlin/backuputility/FileSerializer.kt"
```