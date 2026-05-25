package com.auralyx.di

import com.auralyx.converter.AD17Converter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import android.content.Context
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConverterModule {
    @Provides
    @Singleton
    fun provideAD17Converter(@ApplicationContext ctx: Context): AD17Converter = AD17Converter(ctx)
}
