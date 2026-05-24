package com.auralyx.di

import android.content.Context
import androidx.room.Room
import com.auralyx.data.local.dao.MediaDao
import com.auralyx.data.local.database.AuralyxDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AuralyxDatabase =
        Room.databaseBuilder(ctx, AuralyxDatabase::class.java, AuralyxDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()   // v1 → v2: new columns; safe to rebuild
            .build()

    @Provides
    fun provideDao(db: AuralyxDatabase): MediaDao = db.mediaDao()
}
