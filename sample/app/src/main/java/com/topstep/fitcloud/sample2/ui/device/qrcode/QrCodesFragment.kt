package com.topstep.fitcloud.sample2.ui.device.qrcode

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentQrCodesBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.DeviceFragment
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.launchWithLog
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.features.FcSettingsFeature
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.settings.FcQrCodeType
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/10.Other-Features#qr-codes
 *
 * ***Description**
 * Setting QR Codes to the device
 *
 * **Usage**
 * 1. [DeviceFragment]
 * When either [FcDeviceInfo.Feature.COLLECTION_CODE] or [FcDeviceInfo.Feature.BUSINESS_CARD]
 * or [FcDeviceInfo.Feature.NUCLEIC_ACID_CODE] or [FcDeviceInfo.Feature.QR_CODE_EXTENSION_1]  is supported, the entrance is displayed
 *
 * 2. [QrCodesFragment]
 * Use [FcSettingsFeature.requestSupportQrCodes] to query support QR Codes and use [FcSettingsFeature.setQrCode] to setting them.
 *
 */
class QrCodesFragment : BaseFragment(R.layout.fragment_qr_codes) {

    private val viewBind: FragmentQrCodesBinding by viewBinding()
    private val viewModel: QrCodesViewModel by viewModels()

    private val deviceManager = Injector.getDeviceManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.loadingView.associateViews = arrayOf(viewBind.scrollView)
        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.request()
        }

        viewBind.itemCollectionCodeWechat.clickTrigger(block = blockClick)
        viewBind.itemCollectionCodeAlipay.clickTrigger(block = blockClick)
        viewBind.itemCollectionCodePaypal.clickTrigger(block = blockClick)
        viewBind.itemBusinessCardFacebook.clickTrigger(block = blockClick)
        viewBind.itemBusinessCardWhatsapp.clickTrigger(block = blockClick)
        viewBind.itemBusinessCardTwitter.clickTrigger(block = blockClick)
        viewBind.itemNucleicAcidCode.clickTrigger(block = blockClick)
        viewBind.itemCultSignIn.clickTrigger(block = blockClick)
        viewBind.itemMultiMedia.clickTrigger(block = blockClick)

        lifecycle.launchRepeatOnStarted {
            viewModel.flowState.collect {
                when (it.async) {
                    is Loading -> {
                        viewBind.loadingView.showLoading()
                    }
                    is Success -> {
                        viewBind.loadingView.visibility = View.GONE
                        val result = it.async()!!
                        viewBind.itemCollectionCodeWechat.isVisible = result.collectionCodeWechat
                        viewBind.itemCollectionCodeAlipay.isVisible = result.collectionCodeAliPay
                        viewBind.itemCollectionCodePaypal.isVisible = result.collectionCodePayPal
                        viewBind.itemBusinessCardFacebook.isVisible = result.businessCardFacebook
                        viewBind.itemBusinessCardWhatsapp.isVisible = result.businessCardWhatsApp
                        viewBind.itemBusinessCardTwitter.isVisible = result.businessCardTwitter
                        viewBind.itemNucleicAcidCode.isVisible = result.nucleicAcidCode
                        viewBind.itemCultSignIn.isVisible = result.cultSignIn
                        viewBind.itemMultiMedia.isVisible = result.multiMedia
                    }
                    is Fail -> {
                        viewBind.loadingView.showError(R.string.tip_load_error)
                    }
                    else -> {}
                }
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemCollectionCodeWechat -> {
                lifecycleScope.launchWithLog {
                    deviceManager.settingsFeature.setQrCode(
                        FcQrCodeType.COLLECTION_CODE_WECHAT,
                        "This is a Wechat collection code"
                    ).await()
                }
            }
            viewBind.itemCollectionCodeAlipay -> {
                lifecycleScope.launchWithLog {
                    deviceManager.settingsFeature.setQrCode(
                        FcQrCodeType.COLLECTION_CODE_ALIPAY,
                        "This is a AliPay collection code"
                    ).await()
                }
            }
            viewBind.itemCollectionCodePaypal -> {
                lifecycleScope.launchWithLog {
                    deviceManager.settingsFeature.setQrCode(
                        FcQrCodeType.COLLECTION_CODE_PAYPAL,
                        "This is a PayPal collection code"
                    ).await()
                }
            }
            viewBind.itemBusinessCardFacebook -> {
                lifecycleScope.launchWithLog {
                    deviceManager.settingsFeature.setQrCode(
                        FcQrCodeType.BUSINESS_CARD_FACEBOOK,
                        "This is a Facebook business card"
                    ).await()
                }
            }
            viewBind.itemBusinessCardWhatsapp -> {
                lifecycleScope.launchWithLog {
                    deviceManager.settingsFeature.setQrCode(
                        FcQrCodeType.BUSINESS_CARD_WHATSAPP,
                        "This is a WhatsApp business card"
                    ).await()
                }
            }
            viewBind.itemBusinessCardTwitter -> {
                lifecycleScope.launchWithLog {
                    deviceManager.settingsFeature.setQrCode(
                        FcQrCodeType.BUSINESS_CARD_TWITTER,
                        "This is a Twitter business card"
                    ).await()
                }
            }
            viewBind.itemNucleicAcidCode -> {
                lifecycleScope.launchWithLog {
                    deviceManager.settingsFeature.setQrCode(
                        FcQrCodeType.NUCLEIC_ACID_CODE,
                        "This is a Nucleic Acid Code"
                    ).await()
                }
            }
            viewBind.itemCultSignIn -> {
                lifecycleScope.launchWithLog {
                    deviceManager.settingsFeature.setQrCode(
                        FcQrCodeType.CULT_SIGN_IN,
                        "This is a Cult Sign In Custom QR codes"
                    ).await()
                }
            }
            viewBind.itemMultiMedia -> {
                lifecycleScope.launchWithLog {
                    deviceManager.settingsFeature.setQrCode(
                        FcQrCodeType.MULTI_MEDIA,
                        "This is a Cult Multi Media Custom QR codes"
                    ).await()
                }
            }
        }
    }
}

data class QrCodesSupport(
    val collectionCodeWechat: Boolean,
    val collectionCodeAliPay: Boolean,
    val collectionCodePayPal: Boolean,
    val businessCardFacebook: Boolean,
    val businessCardWhatsApp: Boolean,
    val businessCardTwitter: Boolean,
    val nucleicAcidCode: Boolean,
    val cultSignIn: Boolean,
    val multiMedia: Boolean,
)

class QrCodesViewModel : AsyncViewModel<SingleAsyncState<QrCodesSupport>>(SingleAsyncState()) {

    private val deviceManager = Injector.getDeviceManager()

    init {
        request()
    }

    fun request() {
        suspend {
            val result = deviceManager.settingsFeature.requestSupportQrCodes().await()
            var collectionCodeWechat = false
            var collectionCodeAliPay = false
            var collectionCodePayPal = false
            var businessCardFacebook = false
            var businessCardWhatsApp = false
            var businessCardTwitter = false
            var nucleicAcidCode = false
            var cultSignIn = false
            var multiMedia = false
            for (type in result) {
                when (type) {
                    FcQrCodeType.COLLECTION_CODE_WECHAT -> collectionCodeWechat = true
                    FcQrCodeType.COLLECTION_CODE_ALIPAY -> collectionCodeAliPay = true
                    FcQrCodeType.COLLECTION_CODE_PAYPAL -> collectionCodePayPal = true
                    FcQrCodeType.BUSINESS_CARD_FACEBOOK -> businessCardFacebook = true
                    FcQrCodeType.BUSINESS_CARD_WHATSAPP -> businessCardWhatsApp = true
                    FcQrCodeType.BUSINESS_CARD_TWITTER -> businessCardTwitter = true
                    FcQrCodeType.NUCLEIC_ACID_CODE -> nucleicAcidCode = true
                    FcQrCodeType.CULT_SIGN_IN -> cultSignIn = true
                    FcQrCodeType.MULTI_MEDIA -> multiMedia = true
                }
            }
            QrCodesSupport(
                collectionCodeWechat,
                collectionCodeAliPay,
                collectionCodePayPal,
                businessCardFacebook,
                businessCardWhatsApp,
                businessCardTwitter,
                nucleicAcidCode,
                cultSignIn,
                multiMedia
            )
        }.execute(SingleAsyncState<QrCodesSupport>::async) {
            copy(async = it)
        }
    }
}