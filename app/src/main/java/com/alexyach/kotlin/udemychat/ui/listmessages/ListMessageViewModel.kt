package com.alexyach.kotlin.udemychat.ui.listmessages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alexyach.kotlin.udemychat.domain.MessageModel
import com.alexyach.kotlin.udemychat.domain.UserModel
import com.alexyach.kotlin.udemychat.utils.FIREBASE_MESSAGE
import com.alexyach.kotlin.udemychat.utils.FIREBASE_USERS
import com.alexyach.kotlin.udemychat.utils.LOG_TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/** UI State */
sealed interface MessageListUiState {
    data class Success(val list: List<MessageModel>) : MessageListUiState
    data class Error(val error: DatabaseError) : MessageListUiState
    object Loading : MessageListUiState
}
/** *** */

class ListMessageViewModel : ViewModel() {
    private var database: FirebaseDatabase = Firebase.database
    private var messagesReference: DatabaseReference = database.getReference(FIREBASE_MESSAGE)
    private var usersReference: DatabaseReference = database.getReference(FIREBASE_USERS)

    private val _messageListUiState: MutableLiveData<MessageListUiState> = MutableLiveData()
    val messageListUiState: LiveData<MessageListUiState> = _messageListUiState

    private val _userNameLiveData: MutableLiveData<String> = MutableLiveData()
    val userNameLiveData: LiveData<String> = _userNameLiveData

    var currentUserId = ""


    init {
        _messageListUiState.value = MessageListUiState.Loading
        messageFirebaseListener()
        usersFirebaseListener()
    }

    fun sendMessageToFirebase(message: MessageModel) {
        messagesReference.push().setValue(message)
    }

    fun signOut() {
        Firebase.auth.signOut()
    }

    private fun messageFirebaseListener() {
        messagesReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val listMessage = mutableListOf<MessageModel>()

                for (s in snapshot.children) {
                    val message = s.getValue(MessageModel::class.java)
                    if (message != null) listMessage.add(message)
                }
                _messageListUiState.value = MessageListUiState.Success(listMessage)

            }

            override fun onCancelled(error: DatabaseError) {
                _messageListUiState.value = MessageListUiState.Error(error)
            }

        })
    }

    private fun usersFirebaseListener() {
        usersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (s in snapshot.children) {
                    val user = s.getValue(UserModel::class.java)

                    if (user?.id == currentUserId) {
//                if (user?.id == FirebaseAuth.getInstance().currentUser?.uid) {
                        _userNameLiveData.value = user.name
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _userNameLiveData.value = "No Name"
            }
        })
    }

}

