package io.github.whiredplanck.storageaccess

import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.Closeable

public data class StorageDocument(
    val uri: Uri,
    val name: String,
    val isDir: Boolean,
    val length: Long,
    val lastModified: Long,
    val mimeType: String? = null,
)

public data class StoragePersistedPermission(
    val uri: Uri,
    val read: Boolean,
    val write: Boolean,
    val persistedTime: Long,
) {
    override fun toString(): String = "SafPersistedPermission(uri='$uri', read=$read, write=$write)"
}

public data class StorageFileDescriptor(
    val pfd: ParcelFileDescriptor,
    val path: String,
) : Closeable {
    override fun close() {
        pfd.close()
    }
}

public data class StorageWalkEntry(
    val file: StorageDocument,
    /** POSIX-style path relative to the walk root, e.g. `sub/dir/file.txt`. */
    val relativePath: String,
) {
    override fun toString(): String = "StorageWalkEntry(relativePath='$relativePath')"
}
