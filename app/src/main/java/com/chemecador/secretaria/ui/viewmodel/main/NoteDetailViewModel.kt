package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.model.Note
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.data.repositories.main.MainRepository
import com.chemecador.secretaria.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: MainRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _note = MutableLiveData<Resource<Note>>()
    val note: LiveData<Resource<Note>> get() = _note

    private val _updateStatus = MutableLiveData<Resource<Unit>>()

    private val _deleteStatus = MutableLiveData<Resource<Unit>>()

    fun getNote(listId: String, noteId: String) {
        viewModelScope.launch {
            _note.postValue(Resource.Loading())
            val result = repository.getNote(listId, noteId)
            _note.postValue(result)
        }
    }

    fun deleteNote(listId: String, noteId: String) {
        viewModelScope.launch {
            _deleteStatus.postValue(Resource.Loading())
            val result = repository.deleteNote(listId, noteId)
            _deleteStatus.postValue(result)
        }
    }

    fun editNote(listId: String, note: Note) {
        viewModelScope.launch {
            _updateStatus.postValue(Resource.Loading())
            val result = repository.editNote(listId, note)
            _updateStatus.postValue(result)
        }
    }


    fun getUsername() = userRepository.getUsername() ?: ""
}
