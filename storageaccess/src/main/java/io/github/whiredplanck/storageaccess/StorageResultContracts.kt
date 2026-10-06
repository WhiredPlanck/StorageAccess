package io.github.whiredplanck.storageaccess

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract

public class StorageResultContracts private constructor() {
    public class OpenDirectory : ActivityResultContract<OpenDirectory.Options, Result<StorageDocument?>>() {
        private lateinit var args: Options

        override fun createIntent(
            context: Context,
            input: Options,
        ): Intent {
            args = input
            return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                val initialUri = input.initialUri
                if (initialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
                }
            }
        }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?,
        ): Result<StorageDocument?> {
            if (resultCode != Activity.RESULT_OK || intent == null) {
                return Result.success(null)
            }
            return try {
                val tree = intent.data!!
                val write = args.writePermission
                val persistable = args.persistablePermission
                if (persistable) takePersistable(tree, write)
                val value = StorageDocs.stat(ctx, tree)
                Result.success(value)
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }

        public data class Options(
            val initialUri: Uri?,
            val writePermission: Boolean,
            val persistablePermission: Boolean,
        )
    }

    public data class OpenDocumentOptions(
        val initialUri: Uri?,
        val mimeTypes: List<String>?,
        val persistablePermission: Boolean
    )

    public class OpenFile : ActivityResultContract<OpenDocumentOptions, Result<StorageDocument?>>() {
        private lateinit var opts: OpenDocumentOptions

        override fun createIntent(
            context: Context,
            input: OpenDocumentOptions
        ): Intent {
            opts = input
            return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                val mimeTypes = input.mimeTypes
                if (!mimeTypes.isNullOrEmpty()) {
                    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.toTypedArray())
                }
                val initialUri = input.initialUri
                if (initialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
                }
            }
        }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): Result<StorageDocument?> {
            if (resultCode != Activity.RESULT_OK || intent == null) {
                return Result.success(null)
            }
            return try {
                val tree = intent.data!!
                val persistable = opts.persistablePermission
                if (persistable) takePersistable(tree, write = false)
                val value = StorageDocs.stat(ctx, tree)
                Result.success(value)
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    public class OpenFiles : ActivityResultContract<OpenDocumentOptions, Result<List<StorageDocument>>>() {
        private lateinit var opts: OpenDocumentOptions

        override fun createIntent(
            context: Context,
            input: OpenDocumentOptions
        ): Intent {
            opts = input
            return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                val mimeTypes = input.mimeTypes
                if (!mimeTypes.isNullOrEmpty()) {
                    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.toTypedArray())
                }
                val initialUri = input.initialUri
                if (initialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
                }
            }
        }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): Result<List<StorageDocument>> {
            if (resultCode != Activity.RESULT_OK || intent == null) {
                return Result.success(emptyList())
            }
            return try {
                val persistable = opts.persistablePermission
                // Use a LinkedHashSet to maintain any ordering that may be
                // present in the ClipData
                val resultSet = LinkedHashSet<Uri>()
                intent.data?.let { data ->
                    resultSet.add(data)
                }
                val clipData = intent.clipData
                if (clipData == null && resultSet.isEmpty()) {
                    return Result.success(emptyList())
                } else if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        if (uri != null) {
                            resultSet.add(uri)
                        }
                    }
                }
                val value = resultSet.mapNotNull {
                    if (persistable) takePersistable(it, write = false)
                    StorageDocs.stat(ctx, it)
                }
                return Result.success(value)
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    public class CreateFile : ActivityResultContract<CreateFile.Options, Result<StorageDocument?>>() {
        override fun createIntent(
            context: Context,
            input: Options
        ): Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = input.mimeType
            input.filename?.let { putExtra(Intent.EXTRA_TITLE, it) }
            val initialUri = input.initialUri
            if (initialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            }
        }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): Result<StorageDocument?> {
            if (resultCode != Activity.RESULT_OK || intent == null) {
                return Result.success(null)
            }
            return try {
                val uri = intent.data ?: return Result.success(null)
                val value = StorageDocs.stat(ctx, uri)
                Result.success(value)
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }

        public data class Options(
            val initialUri: Uri?,
            val filename: String?,
            val mimeType: String,
        )
    }

    private companion object {
        private val ctx get() = StorageAccessInitializer.getContext()

        private fun takePersistable(uri: Uri, write: Boolean) {
            var flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            if (write) flags = flags or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            ctx.contentResolver.takePersistableUriPermission(uri, flags)
        }
    }
}
