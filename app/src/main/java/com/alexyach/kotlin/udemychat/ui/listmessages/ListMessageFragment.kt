package com.alexyach.kotlin.udemychat.ui.listmessages

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alexyach.kotlin.udemychat.R
import com.alexyach.kotlin.udemychat.databinding.FragmentListMessageBinding
import com.alexyach.kotlin.udemychat.domain.MessageModel
import com.alexyach.kotlin.udemychat.ui.listmessages.adapter.ListMessageAdapter
import com.alexyach.kotlin.udemychat.ui.listmessages.adapter.RecyclerViewItemDecorator
import com.alexyach.kotlin.udemychat.ui.signin.SignInFragment
import com.alexyach.kotlin.udemychat.utils.KEY_CURRENT_USER_ID
import com.alexyach.kotlin.udemychat.utils.KEY_RECIPIENT_ID
import com.google.firebase.database.DatabaseError

class ListMessageFragment : Fragment() {

    private var _binding: FragmentListMessageBinding? = null
    private val binding: FragmentListMessageBinding get() = _binding!!

    private val viewModel: ListMessageViewModel by lazy {
        ViewModelProvider(this)[ListMessageViewModel::class.java]
    }

    private lateinit var adapter: ListMessageAdapter
    private var listMessage = listOf<MessageModel>()

    private lateinit var currentUserId: String
    private lateinit var recipientUserId: String
    private var recipientName = ""
    private var userName = "Username"

    // UpLoad File From Local
    private val getContext = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            upLoadImage(it)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListMessageBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = arguments?.run {
            getString(KEY_CURRENT_USER_ID)
        } ?: ""
        viewModel.currentUserId = currentUserId

        recipientUserId = arguments?.run {
            getString(KEY_RECIPIENT_ID)
        } ?: ""
        viewModel.recipientUserId = recipientUserId

        setupToolbar()
//        setUpAdapter(listMessage)
        setupRecyclerView()
        setClickButtons()
        changeMessageEditText()
        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.messageListUiState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is MessageListUiState.Success -> showMessagesList(uiState.list)
                MessageListUiState.Loading -> showLoading()
                is MessageListUiState.Error -> showError(uiState.error)
            }
        }

        viewModel.userNameLiveData.observe(viewLifecycleOwner) {
            userName = it
//            Log.d(LOG_TAG, "observe name= $it")
        }

        viewModel.recipientName.observe(viewLifecycleOwner) {
            recipientName = it
            setupToolbar()
        }
    }

    private fun showMessagesList(dataList: List<MessageModel>) {
        binding.progressBarMessageList.visibility = View.GONE
        listMessage = dataList



//        adapter.notifyDataSetChanged()
        setUpAdapter(dataList)
    }

    private fun showLoading() {
        binding.progressBarMessageList.visibility = View.VISIBLE
    }

    private fun showError(error: DatabaseError) {
        binding.progressBarMessageList.visibility = View.GONE
        setUpAdapter(
            listOf(
                MessageModel(
                    name = "ERROR",
                    message = error.message
                )
            )
        )
    }

    private fun setUpAdapter(messageList: List<MessageModel>) {
        adapter = ListMessageAdapter(messageList)
        binding.messagesListRecycler.adapter = adapter
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true // До кінця списку

//        Log.d(LOG_TAG,"${ layoutManager.reverseLayout }")
//        layoutManager.reverseLayout
//        Log.d(LOG_TAG,"${ layoutManager.reverseLayout }")

        binding.messagesListRecycler.layoutManager = layoutManager

        binding.messagesListRecycler.addItemDecoration(RecyclerViewItemDecorator())

//        if (listMessage.isNotEmpty()) {
//            binding.messagesListRecycler.smoothScrollToPosition(adapter.itemCount - 1)
//        }
    }

    private fun setClickButtons() {
        binding.messageSendButton.setOnClickListener {
            sendMessage()
            binding.messageSendEditText.text = null
            hideKeyboard(it)
        }

        binding.photoSendButton.setOnClickListener {
            getContext.launch("image/*")
        }
    }

    private fun upLoadImage(uri: Uri) {
        viewModel.upLoadImageFromFirebase(uri, userName)
    }

    private fun sendMessage() {
        viewModel.sendMessageToFirebase(
            MessageModel(
                name = userName,
                message = binding.messageSendEditText.text.toString(),
                senderId = currentUserId,
                recipientId = recipientUserId
            )
        )
    }

    private fun changeMessageEditText() {
        // Обмеження кількості символів
        binding.messageSendEditText.filters = arrayOf(InputFilter.LengthFilter(500))

        binding.messageSendEditText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Активує кнопку Send
                binding.messageSendButton.isEnabled = s.toString().trim().isNotEmpty()
            }

            // Не використовуємо
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    // Сховати клавіатуру
    private fun hideKeyboard(view: View?) {
        val imm = requireContext()
            .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)

    }

    /** ToolBar */
    private fun setupToolbar() {

        with(binding.toolbarListMessage) {
            title = " $recipientName"
            logo = resources.getDrawable(R.drawable.user_logo, null)
            setBackgroundColor(resources.getColor(R.color.gray_light, null))

            // Menu
            menu.clear()
            inflateMenu(R.menu.menu_sign_out)
            setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.menu_sign_out -> {
                        viewModel.signOut()
                        goToSignInFragment()
                        true
                    }

                    else ->  false
                }
            }
        }
//        binding.toolbarListMessage.
    }

    private fun goToSignInFragment() {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, SignInFragment.newInstance())
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        fun newInstance(userId: String, recipientId: String): ListMessageFragment {
            return ListMessageFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_CURRENT_USER_ID, userId)
                    putString(KEY_RECIPIENT_ID, recipientId)
                }
            }
        }
    }

}

/*
object DateUtil {
    fun formatTime(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(timeInMillis)
    }
    fun formatDate(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
        return dateFormat.format(timeInMillis)
    }
}*/
