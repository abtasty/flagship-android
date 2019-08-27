package com.abtasty.flagship.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [HitData::class, ModificationData::class], version = 2)
@TypeConverters(Converters::class)
abstract class
Database : RoomDatabase() {
    abstract fun hitDao() : HitDao
    abstract fun modificationDao() : ModificationDao
}