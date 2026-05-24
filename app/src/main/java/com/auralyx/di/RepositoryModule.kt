package com.auralyx.di
import com.auralyx.data.repository.MediaRepositoryImpl
import com.auralyx.domain.repository.MediaRepository
import dagger.Binds; import dagger.Module; import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindRepo(impl:MediaRepositoryImpl): MediaRepository
}
