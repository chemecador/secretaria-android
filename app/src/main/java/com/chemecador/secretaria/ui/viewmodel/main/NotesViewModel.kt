package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.data.repositories.main.MainRepository
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: MainRepository,
    private val userRepository: UserRepository,
    private val res: ResourceProvider
) : ViewModel() {

    private val _notes = MutableStateFlow<Resource<List<Note>>>(Resource.Loading())
    val notes: StateFlow<Resource<List<Note>>> = _notes.asStateFlow()

    private val _error = MutableSharedFlow<String>(replay = 0)
    val error: SharedFlow<String> = _error.asSharedFlow()

    fun fetchNotes(listId: String) {
        viewModelScope.launch {
            _notes.value = Resource.Loading()
            when (val result = repository.getNotes(listId)) {
                is Resource.Success -> {
                    _notes.value = result
                }

                is Resource.Error -> {
                    _notes.value = result
                    _error.emit(result.message ?: res.getString(R.string.error_unknown))
                }

                is Resource.Loading -> { /* do nothing */
                }
            }
        }
    }

    fun createNote(listId: String, note: Note) {
        viewModelScope.launch {
            val currentNotes = (_notes.value as? Resource.Success)?.data.orEmpty()
            val nextOrder = if (currentNotes.isEmpty()) 0 else currentNotes.maxOf { it.order } + 1
            val noteWithOrder = note.copy(order = nextOrder)

            when (val result = repository.createNote(listId, noteWithOrder)) {
                is Resource.Success -> {
                    fetchNotes(listId)
                }

                is Resource.Error -> {
                    _error.emit(result.message ?: res.getString(R.string.error_creating_note))
                }

                is Resource.Loading -> { /* do nothing */
                }
            }
        }
    }

    fun reorderNotes(listId: String, reorderedList: List<Note>) {
        viewModelScope.launch {
            _notes.value = Resource.Success(reorderedList)

            val result = repository.reorderNotes(listId, reorderedList)
            if (result is Resource.Error) {
                _error.emit(result.message ?: res.getString(R.string.error_updating_note))
                fetchNotes(listId)
            }
        }
    }

    fun getUsername(): String = userRepository.getUsername() ?: ""
}
