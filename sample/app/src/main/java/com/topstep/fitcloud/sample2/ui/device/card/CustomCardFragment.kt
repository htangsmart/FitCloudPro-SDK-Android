package com.topstep.fitcloud.sample2.ui.device.card

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.recyclerview.CustomDividerItemDecoration
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentCustomCardBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.ui.widget.SwipeItemLayout
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.settings.FcCustomCard
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import java.util.Collections

class CustomCardFragment : BaseFragment(R.layout.fragment_custom_card), CustomCardAddDialogFragment.Listener {

    private val deviceManager = Injector.getDeviceManager()
    private val viewBind: FragmentCustomCardBinding by viewBinding()
    private val adapter: CustomCardAdapter = CustomCardAdapter()
    private var countLimit = 0
    private var contentLimit = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBind.recyclerView.addItemDecoration(CustomDividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
            setIgnoreLastDivider(true)
        })
        viewBind.recyclerView.addOnItemTouchListener(SwipeItemLayout.OnSwipeItemTouchListener(requireContext()))
        adapter.listener = object : CustomCardAdapter.Listener {
            override fun onItemClick(card: FcCustomCard, position: Int) {
                CustomCardAddDialogFragment.newInstance(card, position).show(childFragmentManager, null)
            }

            override fun onItemDelete(position: Int) {
                deleteItem(position)
            }

            override fun onItemSwipe(from: Int, to: Int) {
                swipeItem(from, to)
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)
        viewBind.recyclerView.adapter = adapter
        adapter.itemTouchHelper.attachToRecyclerView(viewBind.recyclerView)

        viewBind.loadingView.listener = LoadingView.Listener {
            requestCards()
        }
        viewBind.loadingView.associateViews = arrayOf(viewBind.recyclerView)

        viewBind.fabAdd.setOnClickListener {
            if (adapter.itemCount >= countLimit) {
                promptToast.showInfo("Up to $countLimit cards can be added.")
            } else {
                CustomCardAddDialogFragment.newInstance().show(childFragmentManager, null)
            }
        }
        requestCards()
    }

    /**
     * Request cards
     */
    private fun requestCards() {
        viewBind.loadingView.showLoading()
        viewBind.fabAdd.hide()

        lifecycleScope.launch {
            try {
                val cardsLimit = deviceManager.settingsFeature.requestCustomCards().await()
                lifecycleScope.launchWhenStarted {
                    if (cardsLimit.cards.isEmpty()) {
                        viewBind.loadingView.showError(R.string.tip_current_no_data)
                    } else {
                        viewBind.loadingView.visibility = View.GONE
                    }

                    this@CustomCardFragment.countLimit = cardsLimit.countLimit
                    this@CustomCardFragment.contentLimit = cardsLimit.contentLimit
                    adapter.setItems(cardsLimit.cards)

                    viewBind.fabAdd.show()
                }
            } catch (e: Exception) {
                Timber.w(e)
                lifecycleScope.launchWhenStarted {
                    viewBind.loadingView.showError(R.string.tip_load_error)
                    viewBind.fabAdd.hide()
                }
            }
        }
    }

    /**
     * Add cards
     */
    override fun onDialogAdd(title: String, content: String) {
        if (title.isEmpty() || content.isEmpty()) {
            promptToast.showInfo("Title or content is empty!")
            return
        }
        if (content.toByteArray().size > contentLimit) {
            promptToast.showInfo("Too much content!")
            return
        }
        promptProgress.showProgress(R.string.tip_please_wait)
        val id = adapter.findNextId()
        lifecycleScope.launch {
            try {
                val card = FcCustomCard(id, title, content)
                Timber.w("set card:$card")
                deviceManager.settingsFeature.setCustomCards(card).await()
                lifecycleScope.launchWhenStarted {
                    adapter.addItem(card)
                    promptProgress.dismiss()
                }
            } catch (e: Exception) {
                Timber.w(e)
                lifecycleScope.launchWhenStarted {
                    promptToast.showFailed(e)
                    promptProgress.dismiss()
                }
            }
        }
    }

    override fun onDialogEdit(card: FcCustomCard, position: Int) {
        if (card.title.isEmpty() || card.content.isEmpty()) {
            promptToast.showInfo("Title or content is empty!")
            return
        }
        if (card.content.toByteArray().size > contentLimit) {
            promptToast.showInfo("Too much content!")
            return
        }
        promptProgress.showProgress(R.string.tip_please_wait)
        lifecycleScope.launch {
            try {
                Timber.w("set card:$card")
                deviceManager.settingsFeature.setCustomCards(card).await()
                lifecycleScope.launchWhenStarted {
                    adapter.editItem(card, position)
                    promptProgress.dismiss()
                }
            } catch (e: Exception) {
                Timber.w(e)
                lifecycleScope.launchWhenStarted {
                    promptToast.showFailed(e)
                    promptProgress.dismiss()
                }
            }
        }
    }

    /**
     * Delete cards
     */
    private fun deleteItem(position: Int) {
        promptProgress.showProgress(R.string.tip_please_wait)
        lifecycleScope.launch {
            try {
                val cardId = adapter.getItems()[position].id
                Timber.w("delete card:$cardId")
                deviceManager.settingsFeature.deleteCustomCards(cardId).await()
                lifecycleScope.launchWhenStarted {
                    adapter.deleteItem(position)
                    promptProgress.dismiss()
                }
            } catch (e: Exception) {
                Timber.w(e)
                lifecycleScope.launchWhenStarted {
                    promptToast.showFailed(e)
                    promptProgress.dismiss()
                }
            }
        }
    }

    /**
     * 交互Item
     */
    private fun swipeItem(from: Int, to: Int) {
        adapter.notifyItemMoved(from, to)
        promptProgress.showProgress(R.string.tip_please_wait)
        lifecycleScope.launch {
            try {
                val list = adapter.getItems().toMutableList()
                Collections.swap(list, from, to)
                val ids = list.map { it.id }.toIntArray()
                Timber.w("sort card:${ids.contentToString()}")
                deviceManager.settingsFeature.sortCustomCards(*ids).await()
                lifecycleScope.launchWhenStarted {
                    adapter.swipeItem(from, to)
                    promptProgress.dismiss()
                }
            } catch (e: Exception) {
                Timber.w(e)
                lifecycleScope.launchWhenStarted {
                    adapter.notifyDataSetChanged()
                    promptToast.showFailed(e)
                    promptProgress.dismiss()
                }
            }
        }
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            if (adapter.itemCount == 0) {
                viewBind.loadingView.showError(R.string.tip_current_no_data)
            } else {
                viewBind.loadingView.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
    }

}