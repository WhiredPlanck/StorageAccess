package io.github.whiredplanck.storageaccess

import android.content.Context
import androidx.startup.Initializer

internal class StorageAccessInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        applicationContext = context.applicationContext ?: context
        checkNotNull(applicationContext) { "Application context cannot be null"}
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> = emptyList()

    companion object {
        private var applicationContext: Context? = null

        fun getContext() = applicationContext!!
    }
}