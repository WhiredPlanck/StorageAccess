package io.github.whiredplanck.storageaccess

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

internal class StorageContextProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        applicationContext = context?.applicationContext ?: context
        checkNotNull(applicationContext) { "Application context cannot be null"}
        return false
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    companion object {
        private var applicationContext: Context? = null

        fun getContext() = applicationContext!!
    }
}