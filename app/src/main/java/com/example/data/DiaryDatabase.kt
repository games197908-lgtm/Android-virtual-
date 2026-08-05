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

@Entity(tableName = "user_ideas")
data class UserIdea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String = "Geral", // e.g. "Tecnologia", "Negócios", "Arte", "Pessoal"
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_notes")
data class LocalNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val noteContent: String,
    val tag: String = "Nota", // e.g. "Importante", "Lembrete", "Rascunho", "Ideia"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "generated_insights")
data class GeneratedInsight(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val insightContent: String,
    val source: String = "Diário & Ideias",
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

@Dao
interface UserIdeaDao {
    @Query("SELECT * FROM user_ideas ORDER BY timestamp DESC")
    fun getAllIdeas(): Flow<List<UserIdea>>

    @Query("SELECT * FROM user_ideas WHERE id = :id")
    suspend fun getIdeaById(id: Int): UserIdea?

    @Query("SELECT * FROM user_ideas WHERE category = :category ORDER BY timestamp DESC")
    fun getIdeasByCategory(category: String): Flow<List<UserIdea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: UserIdea): Long

    @Update
    suspend fun updateIdea(idea: UserIdea)

    @Delete
    suspend fun deleteIdea(idea: UserIdea)

    @Query("DELETE FROM user_ideas WHERE id = :id")
    suspend fun deleteIdeaById(id: Int)

    @Query("DELETE FROM user_ideas")
    suspend fun clearAllIdeas()
}

@Dao
interface LocalNoteDao {
    @Query("SELECT * FROM local_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<LocalNote>>

    @Query("SELECT * FROM local_notes WHERE id = :id")
    suspend fun getNoteById(id: Int): LocalNote?

    @Query("SELECT * FROM local_notes WHERE title LIKE '%' || :query || '%' OR noteContent LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchNotes(query: String): Flow<List<LocalNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: LocalNote): Long

    @Update
    suspend fun updateNote(note: LocalNote)

    @Delete
    suspend fun deleteNote(note: LocalNote)

    @Query("DELETE FROM local_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    @Query("DELETE FROM local_notes")
    suspend fun clearAllNotes()
}

@Dao
interface GeneratedInsightDao {
    @Query("SELECT * FROM generated_insights ORDER BY timestamp DESC")
    fun getAllInsights(): Flow<List<GeneratedInsight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: GeneratedInsight): Long

    @Delete
    suspend fun deleteInsight(insight: GeneratedInsight)

    @Query("DELETE FROM generated_insights WHERE id = :id")
    suspend fun deleteInsightById(id: Int)

    @Query("DELETE FROM generated_insights")
    suspend fun clearAllInsights()
}

// --- database ---

@Database(
    entities = [
        DiaryEntry::class,
        ChatMessage::class,
        UserIdea::class,
        LocalNote::class,
        GeneratedInsight::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun chatDao(): ChatDao
    abstract fun userIdeaDao(): UserIdeaDao
    abstract fun localNoteDao(): LocalNoteDao
    abstract fun generatedInsightDao(): GeneratedInsightDao
}

// --- repository ---

class AppRepository(private val db: AppDatabase) {
    val diaryDao = db.diaryDao()
    val chatDao = db.chatDao()
    val userIdeaDao = db.userIdeaDao()
    val localNoteDao = db.localNoteDao()
    val generatedInsightDao = db.generatedInsightDao()

    val allEntries: Flow<List<DiaryEntry>> = diaryDao.getAllEntries()
    val allMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()
    val allIdeas: Flow<List<UserIdea>> = userIdeaDao.getAllIdeas()
    val allNotes: Flow<List<LocalNote>> = localNoteDao.getAllNotes()
    val allInsights: Flow<List<GeneratedInsight>> = generatedInsightDao.getAllInsights()

    suspend fun insertDiary(entry: DiaryEntry) = diaryDao.insertEntry(entry)
    suspend fun deleteDiary(entry: DiaryEntry) = diaryDao.deleteEntry(entry)
    suspend fun clearDiaries() = diaryDao.clearAllEntries()

    suspend fun insertMessage(message: ChatMessage) = chatDao.insertMessage(message)
    suspend fun clearChatHistory() = chatDao.clearHistory()

    // CRUD User Ideas
    suspend fun insertIdea(idea: UserIdea) = userIdeaDao.insertIdea(idea)
    suspend fun updateIdea(idea: UserIdea) = userIdeaDao.updateIdea(idea)
    suspend fun deleteIdea(idea: UserIdea) = userIdeaDao.deleteIdea(idea)
    suspend fun deleteIdeaById(id: Int) = userIdeaDao.deleteIdeaById(id)
    suspend fun getIdeaById(id: Int) = userIdeaDao.getIdeaById(id)
    suspend fun clearIdeas() = userIdeaDao.clearAllIdeas()

    // CRUD Local Notes
    suspend fun insertNote(note: LocalNote) = localNoteDao.insertNote(note)
    suspend fun updateNote(note: LocalNote) = localNoteDao.updateNote(note)
    suspend fun deleteNote(note: LocalNote) = localNoteDao.deleteNote(note)
    suspend fun deleteNoteById(id: Int) = localNoteDao.deleteNoteById(id)
    suspend fun getNoteById(id: Int) = localNoteDao.getNoteById(id)
    suspend fun searchNotes(query: String) = localNoteDao.searchNotes(query)
    suspend fun clearNotes() = localNoteDao.clearAllNotes()

    // CRUD Generated Insights
    suspend fun insertInsight(insight: GeneratedInsight) = generatedInsightDao.insertInsight(insight)
    suspend fun deleteInsight(insight: GeneratedInsight) = generatedInsightDao.deleteInsight(insight)
    suspend fun deleteInsightById(id: Int) = generatedInsightDao.deleteInsightById(id)
    suspend fun clearInsights() = generatedInsightDao.clearAllInsights()
}
