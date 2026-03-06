package com.biztracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.android.billingclient.api.BillingClient
import com.biztracker.data.local.AppDatabase
import com.biztracker.data.local.BusinessDao
import com.biztracker.data.local.CategoryDao
import com.biztracker.data.local.EntryDao
import com.biztracker.data.repository.BusinessRepository
import com.biztracker.data.repository.CategoryRepository
import com.biztracker.data.repository.EntryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME,
        ).build()
    }

    @Provides
    fun provideBusinessDao(database: AppDatabase): BusinessDao = database.businessDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideEntryDao(database: AppDatabase): EntryDao = database.entryDao()

    @Provides
    @Singleton
    fun provideBusinessRepository(businessDao: BusinessDao): BusinessRepository {
        return BusinessRepository(businessDao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository {
        return CategoryRepository(categoryDao)
    }

    @Provides
    @Singleton
    fun provideEntryRepository(entryDao: EntryDao): EntryRepository {
        return EntryRepository(entryDao)
    }

    @Provides
    @Singleton
    fun provideBillingClient(
        @ApplicationContext context: Context,
    ): BillingClient {
        return BillingClient.newBuilder(context)
            .setListener { _, _ -> }
            .enablePendingPurchases()
            .build()
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(PREFS_NAME) },
        )
    }

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    private const val DB_NAME = "biztracker.db"
    private const val PREFS_NAME = "biztracker_prefs"
}
