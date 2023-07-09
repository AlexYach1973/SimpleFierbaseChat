package com.alexyach.kotlin.udemychat.ui.userlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alexyach.kotlin.udemychat.domain.UserModel
import com.alexyach.kotlin.udemychat.utils.FIREBASE_USERS
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/** UI State */
sealed interface UserListUiState {
    data class Success(val dataList: List<UserModel>) : UserListUiState
    data class Error(val error: DatabaseError) : UserListUiState
    object Loading : UserListUiState
}

class UserListViewModel : ViewModel() {

    private val usersReference: DatabaseReference = Firebase.database.getReference(FIREBASE_USERS)

    private val _userListUiState = MutableLiveData<UserListUiState>()
    val userListUiState: LiveData<UserListUiState> = _userListUiState

    var currentUserId = ""


    init {
        _userListUiState.value = UserListUiState.Loading
        attachUsersDatabaseReferenceListener()
    }


    fun signOut() {
        Firebase.auth.signOut()
    }

    private fun attachUsersDatabaseReferenceListener() {
        usersReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<UserModel>()

                for (s in snapshot.children) {
                    val user = s.getValue(UserModel::class.java)

                    if (user != null && user.id != currentUserId) {
                        userList.add(user)
                    }
                }
                _userListUiState.value = UserListUiState.Success(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                _userListUiState.value = UserListUiState.Error(error)
            }
        })
    }


}