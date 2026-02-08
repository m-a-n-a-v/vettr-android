package com.vettr.android.core.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vettr.android.core.model.VettrTier

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromVettrTier(tier: VettrTier): String {
        return tier.name
    }

    @TypeConverter
    fun toVettrTier(value: String): VettrTier {
        return VettrTier.valueOf(value)
    }
}
