package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- entities ---

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val mood: String, // e.g. "Feliz", "Neutro", "Reflexivo", "Ansioso", "Inspirado"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- daos ---

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)

    @Query("DELETE FROM diary_entries")
    suspend fun clearAllEntries()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

// --- database ---

@Database(entities = [DiaryEntry::class, ChatMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun chatDao(): ChatDao
}

// --- repository ---

class AppRepository(private val db: AppDatabase) {
    val diaryDao = db.diaryDao()
    val chatDao = db.chatDao()

    val allEntries: Flow<List<DiaryEntry>> = diaryDao.getAllEntries()
    val allMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()

    suspend fun insertDiary(entry: DiaryEntry) = diaryDao.insertEntry(entry)
    suspend fun deleteDiary(entry: DiaryEntry) = diaryDao.deleteEntry(entry)
    suspend fun clearDiaries() = diaryDao.clearAllEntries()

    suspend fun insertMessage(message: ChatMessage) = chatDao.insertMessage(message)
    suspend fun clearChatHistory() = chatDao.clearHistory()
}
