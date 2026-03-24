package com.chemecador.secretaria.data.repositories.main

import com.chemecador.secretaria.R
import com.chemecador.secretaria.core.Constants.CONTRIBUTORS
import com.chemecador.secretaria.core.Constants.NOTES
import com.chemecador.secretaria.core.Constants.NOTES_LIST
import com.chemecador.secretaria.core.Constants.USERS
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.model.NotesList
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val res: ResourceProvider
) : MainRepository {

    override suspend fun getLists(): Resource<List<NotesList>> {
        return try {
            val userId =
                userRepository.getUserId()
                    ?: throw IllegalArgumentException(res.getString(R.string.error_user_not_auth))
            val snapshot = firestore.collectionGroup(NOTES_LIST)
                .whereArrayContains(CONTRIBUTORS, userId)
                .get()
                .await()
            val lists = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(NotesList::class.java)?.copy(id = documentSnapshot.id)
            }
            Resource.Success(lists)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_fetching_lists))
        }
    }

    override suspend fun createList(name: String, ordered: Boolean): Resource<Unit> {
        return try {

            val userId = userRepository.getUserId()
            if (userId == null) {
                Timber.e(res.getString(R.string.error_user_not_auth))
                return Resource.Error(res.getString(R.string.error_unknown))
            }
            val username = userRepository.getUsername() ?: res.getString(R.string.label_anonymous)
            val newList = NotesList(
                id = firestore.collection(USERS).document(userId).collection(NOTES_LIST)
                    .document().id,
                name = name,
                contributors = listOf(userId),
                creator = username,
                date = Timestamp.now(),
                ordered = ordered
            )
            firestore.collection(USERS).document(userId).collection(NOTES_LIST)
                .document(newList.id).set(newList).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_unknown))
        }
    }

    override suspend fun editList(updatedList: NotesList): Resource<Unit> {
        return try {
            val userId = userRepository.getUserId()
                ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            val snapshot = firestore.collectionGroup(NOTES_LIST)
                .whereArrayContains(CONTRIBUTORS, userId)
                .get()
                .await()

            val listDocument = snapshot.documents.firstOrNull { it.id == updatedList.id }
                ?: return Resource.Error(res.getString(R.string.error_fetching_lists))

            val list = listDocument.toObject(NotesList::class.java)
            if (list?.contributors?.contains(userId) != true) {
                return Resource.Error(res.getString(R.string.error_insuficient_permissions))
            }

            listDocument.reference.set(updatedList).await()
            Resource.Success(null)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_updating_list))
        }
    }

    override suspend fun deleteList(listId: String): Resource<Unit> {
        return try {
            val userId = userRepository.getUserId()
                ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            val snapshot = firestore.collectionGroup(NOTES_LIST)
                .whereArrayContains(CONTRIBUTORS, userId)
                .get()
                .await()

            val listDocument = snapshot.documents.firstOrNull { it.id == listId }
                ?: return Resource.Error(res.getString(R.string.error_fetching_lists))

            val list = listDocument.toObject(NotesList::class.java)
            if (list?.contributors?.contains(userId) != true) {
                return Resource.Error(res.getString(R.string.error_insuficient_permissions))
            }

            val batch = firestore.batch()
            val notesRef = listDocument.reference.collection(NOTES)
            val notesSnapshot = notesRef.get().await()

            for (document in notesSnapshot.documents) {
                batch.delete(document.reference)
            }

            batch.delete(listDocument.reference)
            batch.commit().await()

            Resource.Success(null)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_deleting_list))
        }
    }

    override suspend fun getNote(listId: String, noteId: String): Resource<Note> {
        return try {
            val userId = userRepository.getUserId()
                ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            val listSnapshot = firestore.collectionGroup(NOTES_LIST)
                .whereArrayContains(CONTRIBUTORS, userId)
                .get()
                .await()

            val listDocument = listSnapshot.documents.firstOrNull { it.id == listId }
                ?: return Resource.Error(res.getString(R.string.error_fetching_lists))

            val snapshot = listDocument.reference.collection(NOTES).document(noteId)
                .get()
                .await()

            val note = snapshot.toObject(Note::class.java)
            if (note != null) {
                Resource.Success(note)
            } else {
                Resource.Error(res.getString(R.string.error_note_not_found))
            }
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_unknown))
        }
    }

    override suspend fun getNotes(listId: String): Resource<List<Note>> {
        return try {
            val userId = userRepository.getUserId()
                ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            val listSnapshot = firestore.collectionGroup(NOTES_LIST)
                .whereArrayContains(CONTRIBUTORS, userId)
                .get()
                .await()

            val listDocument = listSnapshot.documents.firstOrNull { it.id == listId }
                ?: return Resource.Error(res.getString(R.string.error_fetching_lists))

            val snapshot = listDocument.reference.collection(NOTES)
                .get()
                .await()

            val notes = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Note::class.java)?.copy(id = documentSnapshot.id)
            }
            Resource.Success(notes)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_unknown))
        }
    }

    override suspend fun createNote(listId: String, note: Note): Resource<Unit> {
        return try {
            val userId = userRepository.getUserId()
                ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            val listSnapshot = firestore.collectionGroup(NOTES_LIST)
                .whereArrayContains(CONTRIBUTORS, userId)
                .get()
                .await()

            val listDocument = listSnapshot.documents.firstOrNull { it.id == listId }
                ?: return Resource.Error(res.getString(R.string.error_fetching_lists))

            val newNote = note.copy(
                id = listDocument.reference.collection(NOTES).document().id,
                creator = note.creator,
                color = note.color
            )
            listDocument.reference.collection(NOTES).document(newNote.id).set(newNote).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_unknown))
        }
    }

    override suspend fun editNote(listId: String, note: Note): Resource<Unit> {
        return try {
            val userId = userRepository.getUserId()
                ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            val listSnapshot = firestore.collectionGroup(NOTES_LIST)
                .whereArrayContains(CONTRIBUTORS, userId)
                .get()
                .await()

            val listDocument = listSnapshot.documents.firstOrNull { it.id == listId }
                ?: return Resource.Error(res.getString(R.string.error_fetching_lists))

            listDocument.reference.collection(NOTES).document(note.id).set(note).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_unknown))
        }
    }

    override suspend fun deleteNote(listId: String, noteId: String): Resource<Unit> {
        return try {
            val userId = userRepository.getUserId()
                ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            val listSnapshot = firestore.collectionGroup(NOTES_LIST)
                .whereArrayContains(CONTRIBUTORS, userId)
                .get()
                .await()

            val listDocument = listSnapshot.documents.firstOrNull { it.id == listId }
                ?: return Resource.Error(res.getString(R.string.error_fetching_lists))

            listDocument.reference.collection(NOTES).document(noteId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_deleting_note))
        }
    }

    override suspend fun reorderNotes(listId: String, notes: List<Note>): Resource<Unit> {
        return try {
            val userId = userRepository.getUserId()
                ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            val listSnapshot = firestore.collectionGroup(NOTES_LIST)
                .whereArrayContains(CONTRIBUTORS, userId)
                .get()
                .await()

            val listDocument = listSnapshot.documents.firstOrNull { it.id == listId }
                ?: return Resource.Error(res.getString(R.string.error_fetching_lists))

            val batch = firestore.batch()
            notes.forEachIndexed { index, note ->
                val noteRef = listDocument.reference.collection(NOTES).document(note.id)
                batch.update(noteRef, "order", index)
            }
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_updating_note))
        }
    }

    override suspend fun getContributors(listId: String): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = userRepository.getUserId()
                ?: return@flow emit(Resource.Error(res.getString(R.string.error_user_not_auth)))

            val snapshot = firestore.collectionGroup(NOTES_LIST)
                .whereArrayContains(CONTRIBUTORS, userId)
                .get()
                .await()

            val listDoc = snapshot.documents.firstOrNull { it.id == listId }
                ?: return@flow emit(Resource.Error(res.getString(R.string.error_fetching_lists)))

            @Suppress("UNCHECKED_CAST")
            val contributors = listDoc.get(CONTRIBUTORS) as? List<String> ?: emptyList()

            emit(Resource.Success(contributors))
        } catch (e: Exception) {
            Timber.e(e)
            emit(Resource.Error(res.getString(R.string.error_unknown)))
        }
    }.flowOn(Dispatchers.IO)


    override suspend fun shareList(listId: String, friendId: String): Resource<Unit> {
        return try {
            val userId = userRepository.getUserId()
                ?: return Resource.Error(res.getString(R.string.error_user_not_auth))
            val listRef = firestore.collection(USERS).document(userId)
                .collection(NOTES_LIST).document(listId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(listRef)

                @Suppress("UNCHECKED_CAST")
                val contributors =
                    snapshot.get(CONTRIBUTORS) as? MutableList<String> ?: mutableListOf()
                if (!contributors.contains(friendId)) {
                    contributors.add(friendId)
                    transaction.update(listRef, CONTRIBUTORS, contributors)
                }
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_adding_contributor))
        }
    }

    override suspend fun unshareList(
        listId: String,
        friendId: String
    ): Resource<Unit> {
        return try {
            val userId = userRepository.getUserId()
                ?: return Resource.Error(res.getString(R.string.error_user_not_auth))

            val listRef = firestore.collection(USERS)
                .document(userId)
                .collection(NOTES_LIST)
                .document(listId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(listRef)

                @Suppress("UNCHECKED_CAST")
                val contributors =
                    snapshot.get(CONTRIBUTORS) as? MutableList<String> ?: mutableListOf()

                if (contributors.contains(friendId)) {
                    contributors.remove(friendId)
                    transaction.update(listRef, CONTRIBUTORS, contributors)
                }
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e)
            Resource.Error(res.getString(R.string.error_removing_contributor))
        }
    }
}
