package com.nia.compose.bridge.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nia.compose.bridge.model.DemoItem

@Entity(tableName = "demo_items")
data class DemoItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconUrl: String?,
)

fun DemoItemEntity.asExternalModel(): DemoItem = DemoItem(
    id = id,
    title = title,
    description = description,
    iconUrl = iconUrl,
)

fun DemoItem.asEntity(): DemoItemEntity = DemoItemEntity(
    id = id,
    title = title,
    description = description,
    iconUrl = iconUrl,
)
