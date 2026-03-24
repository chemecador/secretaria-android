package com.chemecador.secretaria.ui.viewmodel.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.local.UserPreferences
import com.chemecador.secretaria.data.model.Friend
import com.chemecador.secretaria.data.model.Friendship
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.repositories.friends.FriendsRepository
import com.chemecador.secretaria.data.services.AuthService
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: FriendsRepository,
    private val userPreferences: UserPreferences,
    private val authService: AuthService,
    private val res: ResourceProvider
) : ViewModel() {


    private val _friendships = MutableStateFlow<Resource<List<Friendship>>>(Resource.Loading())
    val friendships: StateFlow<Resource<List<Friendship>>> = _friendships.asStateFlow()

    private val _friends = MutableStateFlow<Resource<List<Friend>>>(Resource.Loading())
    val friends: StateFlow<Resource<List<Friend>>> = _friends.asStateFlow()

    private val _friendRequests = MutableStateFlow<Resource<List<Friendship>>>(Resource.Loading())
    val friendRequests: StateFlow<Resource<List<Friendship>>> = _friendRequests.asStateFlow()

    private val _friendRequestsSent =
        MutableStateFlow<Resource<List<Friendship>>>(Resource.Loading())

    val friendRequestsSent: StateFlow<Resource<List<Friendship>>> =
        _friendRequestsSent.asStateFlow()

    private val _userCode = MutableStateFlow<String?>(null)
    val userCode: StateFlow<String?> = _userCode.asStateFlow()

    private val _deleteFriendStatus = MutableSharedFlow<Resource<Unit>>(replay = 1)
    val deleteFriendStatus: SharedFlow<Resource<Unit>> = _deleteFriendStatus.asSharedFlow()

    private val _acceptRequestStatus = MutableSharedFlow<Resource<Unit>>(replay = 1)
    val acceptRequestStatus: SharedFlow<Resource<Unit>> = _acceptRequestStatus.asSharedFlow()

    private val _rejectRequestStatus = MutableSharedFlow<Resource<Unit>>(replay = 1)
    val rejectRequestStatus: SharedFlow<Resource<Unit>> = _rejectRequestStatus.asSharedFlow()

    private val _addFriendStatus = MutableSharedFlow<Resource<Unit>>(replay = 1)
    val addFriendStatus: SharedFlow<Resource<Unit>> = _addFriendStatus.asSharedFlow()

    private val _cancelRequestStatus = MutableSharedFlow<Resource<Unit>>(replay = 1)


    fun getCurrentUserId() = FirebaseAuth.getInstance().currentUser?.uid

    fun loadFriendships() {
        viewModelScope.launch {
            repository.getFriendships()
                .onStart { _friendships.value = Resource.Loading() }
                .collect { _friendships.value = it }
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            repository.getFriends()
                .onStart { _friends.value = Resource.Loading() }
                .collect { _friends.value = it }
        }
    }

    fun loadFriendRequests() {
        viewModelScope.launch {
            repository.getFriendRequests()
                .onStart { _friendRequests.value = Resource.Loading() }
                .collect { _friendRequests.value = it }
        }
    }

    fun loadFriendRequestsSent() {
        viewModelScope.launch {
            repository.getFriendRequestsSent()
                .onStart { _friendRequestsSent.value = Resource.Loading() }
                .catch { e ->
                    _friendRequestsSent.value = Resource.Error(e.localizedMessage ?: "Error")
                }
                .collect { _friendRequestsSent.value = it }
        }
    }

    fun deleteFriend(friendshipId: String) {
        viewModelScope.launch {
            _deleteFriendStatus.emit(repository.deleteFriend(friendshipId))
        }
    }

    fun acceptFriendRequest(requestId: String) {
        viewModelScope.launch {
            _acceptRequestStatus.emit(repository.acceptFriendRequest(requestId))
        }
    }

    fun rejectFriendRequest(requestId: String) {
        viewModelScope.launch {
            _rejectRequestStatus.emit(repository.rejectFriendRequest(requestId))
        }
    }

    fun cancelFriendRequest(requestId: String) {
        viewModelScope.launch {
            _cancelRequestStatus.emit(repository.cancelFriendRequest(requestId))
        }
    }

    fun sendFriendRequest(friendCode: String) {
        viewModelScope.launch {
            _addFriendStatus.emit(Resource.Loading())
            val friendId = repository.getUserIdByUserCode(friendCode)
            if (friendId == null) {
                _addFriendStatus.emit(Resource.Error(res.getString(R.string.error_user_not_found)))
                return@launch
            }
            if (repository.checkIfAlreadyFriends(friendId)) {
                _addFriendStatus.emit(Resource.Error(res.getString(R.string.error_already_friends)))
                return@launch
            }
            _addFriendStatus.emit(repository.sendFriendRequest(friendCode))
        }
    }

    fun loadUserCode() {
        viewModelScope.launch {
            val saved = userPreferences.userCodeFlow.firstOrNull()
            if (saved != null) {
                _userCode.value = saved
            } else {
                val newCode = authService.getUserCode()
                if (newCode != null) {
                    userPreferences.saveUserCode(newCode)
                    _userCode.value = newCode
                } else {
                    _userCode.value = null
                }
            }
        }
    }
}
