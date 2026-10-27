package com.yldefonso.ojodchakra.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yldefonso.ojodchakra.data.local.DiagnosticoDao
import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.data.model.Parcela
import com.yldefonso.ojodchakra.data.model.Usuario

@Database(
    entities = [Usuario::class, Parcela::class, Diagnostico::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun diagnosticoDao(): DiagnosticoDao
    abstract fun parcelaDao(): ParcelaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Versión 1 a 2: la parcela gana un texto descriptivo del lugar
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE parcelas ADD COLUMN referencia TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ojodchakra.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}