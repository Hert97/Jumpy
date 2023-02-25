package com.jumpy.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class Score(@PrimaryKey(autoGenerate = true) var id: Int,
                 @ColumnInfo(name = "value") var value: Int)