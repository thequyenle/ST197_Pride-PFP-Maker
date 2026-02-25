package com.pfp.pride.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pfp.pride.data.local.dao.UserDao
import com.pfp.pride.data.local.entity.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}