package com.chemecador.secretaria.data.repositories.main

import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.utils.Resource
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun getLists(): Resource<List<NotesList>>
    suspend fun createList(name: String, ordered: Boolean = false): Resource<Unit>
    suspend fun editList(updatedList: NotesList): Resource<Unit>
    suspend fun deleteList(listId: String): Resource<Unit>
    suspend fun createNote(listId: String, note: Note): Resource<Unit>
    suspend fun getNotes(listId: String): Resource<List<Note>>
    suspend fun getNote(listId: String, noteId: String): Resource<Note>
    suspend fun editNote(listId: String, note: Note): Resource<Unit>
    suspend fun deleteNote(listId: String, noteId: String): Resource<Unit>
    suspend fun reorderNotes(listId: String, notes: List<Note>): Resource<Unit>
    suspend fun getContributors(listId: String): Flow<Resource<List<String>>>
    suspend fun shareList(listId: String, friendId: String): Resource<Unit>
    suspend fun unshareList(listId: String, friendId: String): Resource<Unit>
}
