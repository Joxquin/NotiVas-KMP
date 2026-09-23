package me.joxquin.notivas.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

expect fun getDatabaseBuilder(): RoomDatabase.Builder<NotivasDatabase>

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        fun safeExec(sql: String) {
            try {
                connection.execSQL(sql)
            } catch (_: Throwable) {}
        }

        // 1. Courses migration
        safeExec("CREATE TABLE IF NOT EXISTS courses_new (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `course_code` TEXT, `enrollment_term_id` INTEGER, `total_weeks` INTEGER NOT NULL DEFAULT 18, `passing_grade` REAL NOT NULL DEFAULT 12.0, PRIMARY KEY(`id`))")
        safeExec("INSERT OR IGNORE INTO courses_new (`id`, `name`, `course_code`, `enrollment_term_id`) SELECT `id`, `name`, `course_code`, `enrollment_term_id` FROM courses")
        safeExec("DROP TABLE courses")
        safeExec("ALTER TABLE courses_new RENAME TO courses")

        // 2. Simulation groups migration
        safeExec("CREATE TABLE IF NOT EXISTS simulation_groups_new (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `course_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `weight_percentage` REAL NOT NULL, `target_assessments` INTEGER NOT NULL DEFAULT 1, `drop_lowest` INTEGER NOT NULL DEFAULT 0, `min_to_drop` INTEGER NOT NULL DEFAULT 3, `calculation_mode` TEXT NOT NULL DEFAULT 'SIMPLE', `order_index` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`course_id`) REFERENCES `courses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        safeExec("INSERT OR IGNORE INTO simulation_groups_new (`id`, `course_id`, `name`, `weight_percentage`) SELECT `id`, `course_id`, `name`, `weight_percentage` FROM simulation_groups")
        safeExec("DROP TABLE simulation_groups")
        safeExec("ALTER TABLE simulation_groups_new RENAME TO simulation_groups")
        safeExec("CREATE INDEX IF NOT EXISTS `index_simulation_groups_course_id` ON `simulation_groups` (`course_id`)")
        safeExec("CREATE INDEX IF NOT EXISTS `index_simulation_groups_order_index` ON `simulation_groups` (`order_index`)")

        // 3. Simulation items migration
        safeExec("CREATE TABLE IF NOT EXISTS simulation_items_new (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `group_id` INTEGER NOT NULL, `canvas_assignment_id` INTEGER, `name` TEXT NOT NULL, `is_placeholder` INTEGER NOT NULL DEFAULT 0, `week_number` INTEGER, `manual_score` REAL, `simulated_score` REAL NOT NULL DEFAULT 0.0, `is_simulated` INTEGER NOT NULL DEFAULT 0, `max_score` REAL NOT NULL DEFAULT 20.0, `internal_weight` REAL NOT NULL DEFAULT 1.0, `order_index` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`group_id`) REFERENCES `simulation_groups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`canvas_assignment_id`) REFERENCES `assignments`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        safeExec("INSERT OR IGNORE INTO simulation_items_new (`id`, `group_id`, `canvas_assignment_id`, `name`, `is_placeholder`, `simulated_score`, `max_score`) SELECT `id`, `group_id`, `canvas_assignment_id`, `name`, `is_placeholder`, `simulated_score`, `max_score` FROM simulation_items")
        safeExec("DROP TABLE simulation_items")
        safeExec("ALTER TABLE simulation_items_new RENAME TO simulation_items")
        safeExec("CREATE INDEX IF NOT EXISTS `index_simulation_items_group_id` ON `simulation_items` (`group_id`)")
        safeExec("CREATE INDEX IF NOT EXISTS `index_simulation_items_canvas_assignment_id` ON `simulation_items` (`canvas_assignment_id`)")
        safeExec("CREATE INDEX IF NOT EXISTS `index_simulation_items_week_number` ON `simulation_items` (`week_number`)")
        safeExec("CREATE INDEX IF NOT EXISTS `index_simulation_items_order_index` ON `simulation_items` (`order_index`)")
    }
}

fun createRoomDatabase(
    builder: RoomDatabase.Builder<NotivasDatabase> = getDatabaseBuilder()
): NotivasDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .build()
}
