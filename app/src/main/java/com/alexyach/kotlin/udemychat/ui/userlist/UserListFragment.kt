package com.alexyach.kotlin.udemychat.ui.userlist

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.alexyach.kotlin.udemychat.MainActivity
import com.alexyach.kotlin.udemychat.R
import com.alexyach.kotlin.udemychat.databinding.FragmentUserListBinding
import com.alexyach.kotlin.udemychat.domain.MessageModel
import com.alexyach.kotlin.udemychat.domain.UserModel
import com.alexyach.kotlin.udemychat.ui.listmessages.ListMessageAdapter
import com.alexyach.kotlin.udemychat.ui.listmessages.ListMessageFragment
import com.alexyach.kotlin.udemychat.ui.signin.SignInFragment
import com.alexyach.kotlin.udemychat.utils.KEY_CURRENT_USER_ID

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding: FragmentUserListBinding get() = _binding!!

    private val viewModel: UserListViewModel by lazy {
        ViewModelProvider(this)[UserListViewModel::class.java]
    }

    private lateinit var adapter: UserListAdapter

    //    private var userList = listOf<UserModel>()
    private lateinit var currentUserId: String
//    private lateinit var recipientId: String

//    private val currentMessage = MessageModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = arguments?.run {
            getString(KEY_CURRENT_USER_ID)
        } ?: ""
        viewModel.currentUserId = currentUserId
//        currentMessage.copy(
//            senderId = currentUserId
//        )

//        (context as MainActivity).actionBar?.title = "User List"

//        setAdapter(userList)
        observeLiveData()

    }

    private fun observeLiveData() {
        viewModel.userListUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UserListUiState.Success -> showUsersList(state.dataList)
                is UserListUiState.Error -> showError(state.error.message)
                UserListUiState.Loading -> showLoading()
            }


        }
    }

    private fun showUsersList(dataList: List<UserModel>) {
        binding.progressBarUserList.visibility = View.GONE
        setAdapter(dataList)
        //            adapter.notifyDataSetChanged()
    }

    private fun showError(error: String) {
        binding.progressBarUserList.visibility = View.GONE
        setAdapter(
            listOf(
                UserModel(
                    name = error
                )
            )
        )
    }

    private fun showLoading() {
        binding.progressBarUserList.visibility = View.VISIBLE
    }

    private fun setAdapter(dataList: List<UserModel>) {
        adapter = UserListAdapter(dataList, object : OnUserListItemClickListener {
            override fun onUserListItemClick(position: Int) {
                goToChat(dataList[position].id)
//                Toast.makeText(requireContext(), "User id: ${dataList[position].id}", Toast.LENGTH_SHORT).show()
            }
        })
        binding.userListRecyclerView.adapter = adapter
        binding.userListRecyclerView
            .addItemDecoration(DividerItemDecoration(
                requireActivity(),
            DividerItemDecoration.VERTICAL))
    }

    private fun goToChat(recipientId: String) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.container,
                ListMessageFragment.newInstance(
                    viewModel.currentUserId,
                    recipientId
                )
            )
            .addToBackStack(null)
            .commit()
    }


    /** Menu */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sign_out, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sign_out -> {
                viewModel.signOut()
                goToSignInFragment()
            }
        }
        return true
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
        fun newInstance(userId: String): UserListFragment {
            return UserListFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_CURRENT_USER_ID, userId)
                }
            }
        }
    }

}