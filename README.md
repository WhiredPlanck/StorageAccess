# StorageAccess

<p align="left">
  <a href="https://github.com/WhiredPlanck/StorageAccess/issues"><img src="https://img.shields.io/github/issues/WhiredPlanck/StorageAccess">
  </a>
  <img src="https://img.shields.io/github/license/WhiredPlanck/StorageAccess">
</p>

## Overview

One library for the Android Storage Access Framework: pickers, persisted
permissions, file management with recursive walk, streamed read/write, file descriptors, and local-file bridging — all in
a single `StorageAccess` class. Ask the user once, keep the grant forever, and work
painless with their files in a handful of lines instead of hundreds of lines of
template code with `DocumentContract`.

## Highlights

- **One class, no ceremony** — pickers, permissions, file management, and I/O all on `StorageAccess`; no `isDir` parameters anywhere.
- **Typed errors** — `StorageNotFoundException`, `StorageAlreadyExistsException`.
- **Recursive `walk()`** plus recursive `copyTo` / `moveTo`.
- **Streaming I/O** — `readFile` with InputStream and `writeFile` with OutputStream, plus backpressured `readFileStream` and one-call `writeFileStream` for large files.
- **File descriptors** — Closeable `fileDescriptor` hands native or path-based APIs a live `/proc/self/fd/<fd>`.
- **Persisted permissions** — grant once, reuse across restarts; list them with `persistedPermissions()`.
- **Hidden folders** — read dotfile folders (e.g. WhatsApp `.Statuses`) and pull them into your app dir with `copyDirToLocal`.
- **Broad support** — Android minSdk 21.

## Quick start

```kotlin
import io.planck.storageaccess.StorageAccess

// 1. Ask once in ActivityResultCaller like ComponentActivty or Fragment
class MainActivity: ComponentActivity() {

    private val storageAccess = StroageAccess(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnPick.setOnClickListener {
            lifecycleScope.launch {
                val dir = storageAccess.pickDirectory()
                if (dir == null) return@launch // user cancelled

                // do something
            }
        }
    }
}

// Later launches: reuse the grant instead of prompting again.
val grants = StorageAccess.persistedPermissions()

lifecycleScope.launch {
    // 2. Manage files.
    val files = StorageAccess.list(dir.uri)
    val report = StroageAccess.mkdirp(dir.uri, "reports", "2026")
    StorageAccess.walk(dir.uri).collect { entry ->
        println(entry.replativePath)
    }

    // 3. Read and write.
    val doc = StorageAccess.writeFileBytes(
        report.uri, "summary.txt", "text/plain", "hi".toBtyeArray()
    )
    // 3.1 Read btyes
    val bytes = StorageAccess.readFileBytes(doc.uri)
    // 3.2. Read large file with InputStream
    StorageAccess.readFile(doc.uri) { ins -> 
        // do something
    }
    // 3.3 Read large file with Kotlin Flow
    val stream = StorageAccess.readFileStream(doc.uri)
    stream.collect { data -> /** do something */ }

    // 4. Bridge to real file paths when another API needs one.
    StorageAccess.copyToLocalFile(doc.uri, "${cacheDir.path}/summary.txt")

    // 5. Hand a SAF file to anything that wants a real path or fd —
    //    video players, PDF renderers, sqlite — no copy, auto-closed.
    val title = StorageAccess.fileDescriptor(doc.uri, "r").use { fd ->
        someNativeLib.readMetadata(fd.path) // /proc/self/fd/<fd>
    }
}
```

Errors are typed — catch what you care about:

```kotlin
try {
    StorageAccess.delete(uri)
} catch (e: SecurityException) {
    // re-pick the directory
} catch (e: StorageNotFoundException) {
    // already gone
}
```

## Credits

- [`saf`](https://github.com/lognjais/saf) - A Flutter plugin that leverages SAF API to get access and perform the operations on files and folders, developed by [`lognjais (jvoltci)`](https://github.com/lognjais). Referenced for API design (base on v2 API) and README.
- [`SimpleStorage`](https://github.com/anggrayudi/SimpleStorage) - Another library to help simplify Android SAF for file management, developed by [anggrayudi](https://github.com/anggrayudi). Referenced for pickers and dedicated ActivityResultContract implementations.

---

<p align="center">
  <sub>Built &amp; maintained by <a href="https://github.com/WhiredPlanck"><b>WhiredPlanck</b></a> &nbsp;·&nbsp; <a href="https://github.com/lognjais/saf/issues">Issues</a> &nbsp;·&nbsp; Apache 2.0 License</sub>
</p>
<p align="center"><sub>⭐ If <code>StorageAccess</code> saves you time, star the repo — it helps other Android devs find it.</sub></p>

