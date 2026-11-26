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
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun diagnosticoDao(): DiagnosticoDao
    abstract fun parcelaDao(): ParcelaDao
    abstract fun sincronizacionDao(): SincronizacionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Versión 1 a 2: la parcela gana un texto descriptivo del lugar
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE parcelas ADD COLUMN referencia TEXT NOT NULL DEFAULT ''")
            }
        }

        // Versión 2 a 3: latitud y longitud pasan a ser opcionales.
        // SQLite no permite quitar NOT NULL con ALTER TABLE, así que la tabla se reconstruye.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS parcelas_nueva")
                db.execSQL(
                    """
                    CREATE TABLE parcelas_nueva (
                        id TEXT NOT NULL PRIMARY KEY,
                        usuario_id TEXT NOT NULL,
                        nombre TEXT NOT NULL,
                        cultivo TEXT NOT NULL,
                        referencia TEXT NOT NULL,
                        latitud REAL,
                        longitud REAL,
                        fecha_registro INTEGER NOT NULL,
                        estado_sincronizacion TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO parcelas_nueva
                        (id, usuario_id, nombre, cultivo, referencia, latitud, longitud, fecha_registro, estado_sincronizacion)
                    SELECT id, usuario_id, nombre, cultivo, referencia, latitud, longitud, fecha_registro, estado_sincronizacion
                    FROM parcelas
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE parcelas")
                db.execSQL("ALTER TABLE parcelas_nueva RENAME TO parcelas")
            }
        }

        // Versión 3 a 4: columna de corrección (se reemplaza en la 5, la dejamos para no saltarnos pasos)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diagnosticos ADD COLUMN etiqueta_corregida TEXT")
            }
        }

        // Versión 4 a 5: la corrección del agricultor se reemplaza por la revisión del agrónomo.
        // Se reconstruye la tabla para quitar etiqueta_corregida y agregar los campos de revisión.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS diagnosticos_nueva")
                db.execSQL(
                    """
                    CREATE TABLE diagnosticos_nueva (
                        id TEXT NOT NULL PRIMARY KEY,
                        usuario_id TEXT NOT NULL,
                        parcela_id TEXT,
                        ruta_foto TEXT NOT NULL,
                        plaga_detectada TEXT NOT NULL,
                        confianza REAL NOT NULL,
                        nivel_severidad TEXT,
                        recomendacion TEXT,
                        fecha_captura INTEGER NOT NULL,
                        estado_sincronizacion TEXT NOT NULL,
                        estado_revision TEXT NOT NULL,
                        etiqueta_validada TEXT,
                        observacion_agronomo TEXT,
                        fecha_revision INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO diagnosticos_nueva
                        (id, usuario_id, parcela_id, ruta_foto, plaga_detectada, confianza,
                         nivel_severidad, recomendacion, fecha_captura, estado_sincronizacion, estado_revision)
                    SELECT id, usuario_id, parcela_id, ruta_foto, plaga_detectada, confianza,
                           nivel_severidad, recomendacion, fecha_captura, estado_sincronizacion, 'NINGUNA'
                    FROM diagnosticos
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE diagnosticos")
                db.execSQL("ALTER TABLE diagnosticos_nueva RENAME TO diagnosticos")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ojodchakra.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
