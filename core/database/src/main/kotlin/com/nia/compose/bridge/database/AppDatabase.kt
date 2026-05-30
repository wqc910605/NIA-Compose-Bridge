package com.nia.compose.bridge.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nia.compose.bridge.database.dao.DemoItemDao
import com.nia.compose.bridge.database.model.DemoItemEntity

@Database(
    entities = [DemoItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun demoItemDao(): DemoItemDao
}
