package com.auralyx.data.local.database

import androidx.room.*
import com.auralyx.data.local.dao.MediaDao
import com.auralyx.data.local.entity.MediaEntity

@Database(
    entities = [MediaEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AuralyxDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    companion object {
        const val DATABASE_NAME = "auralyx.db"
    }
}
