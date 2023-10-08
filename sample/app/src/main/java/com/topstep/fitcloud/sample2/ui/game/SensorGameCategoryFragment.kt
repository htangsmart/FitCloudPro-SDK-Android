package com.topstep.fitcloud.sample2.ui.game

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kilnn.tool.ui.DisplayUtil
import com.github.kilnn.tool.util.LocalUtil
import com.topstep.fitcloud.sample2.MyApplication
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentSensorGameCategoryBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.widget.GridSimpleSpaceDecoration
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.sg.*
import com.zhpan.bannerview.BannerViewPager
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import java.util.*

class SensorGameCategoryFragment : BaseFragment(R.layout.fragment_sensor_game_category) {

    private val viewBind: FragmentSensorGameCategoryBinding by viewBinding()
    private val viewModel: SensorGameCategoryViewModel by viewModels()

    private lateinit var bannerAdapter: GameBannerAdapter
    private lateinit var categoryAdapter: GameCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bannerAdapter = GameBannerAdapter()
        categoryAdapter = GameCategoryAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = viewBind.bannerView as BannerViewPager<FcSensorGameBanner>
        viewPager.adapter = bannerAdapter
        viewPager.registerLifecycleObserver(viewLifecycle)
        viewPager.create()

        val spaceVertical = DisplayUtil.dip2px(requireContext(), 16f)

        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBind.recyclerView.addItemDecoration(GridSimpleSpaceDecoration(spaceVertical, 0))
        viewBind.recyclerView.adapter = categoryAdapter

        categoryAdapter.listener = {
            viewModel.deviceManager.sensorGameFeature.startGame(requireActivity(), it)
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {

                    when (it.async) {
                        Uninitialized, is Loading -> {
                            viewBind.loadingView.showLoading()
                        }
                        is Success<*> -> {
                            viewBind.loadingView.visibility = View.GONE
                            val data = it.async()
                            viewPager.refreshData(data?.bannerList)

                            categoryAdapter.sources = data?.categoryList
                            categoryAdapter.notifyDataSetChanged()
                        }
                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        categoryAdapter.listener = null
    }

}

class SensorGameCategoryViewModel : AsyncViewModel<SingleAsyncState<FcSensorGames>>(SingleAsyncState()) {

    private val userInfoRepository = Injector.getUserInfoRepository()
    val deviceManager = Injector.getDeviceManager()

    init {
        viewModelScope.launch {
            try {
                //Set Language
                if (LocalUtil.isZh(MyApplication.instance)) {
                    deviceManager.sensorGameFeature.setLanguage(FcSensorGameLanguage.CHINESE)
                } else {
                    deviceManager.sensorGameFeature.setLanguage(FcSensorGameLanguage.ENGLISH)
                }

                //Set UserInfo
                val userInfo = userInfoRepository.flowCurrent.value
                if (userInfo != null) {
                    deviceManager.sensorGameFeature.setUserInfo(
                        userInfo.id.toString(),
                        userInfo.name,
                        ""
                    )
                }
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
        refresh()
    }

    fun refresh() {
        suspend {
            deviceManager.sensorGameFeature.requestGames().await()
        }.execute(SingleAsyncState<FcSensorGames>::async) {
            copy(async = it)
        }
    }

}