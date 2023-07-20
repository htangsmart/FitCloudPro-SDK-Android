package com.topstep.fitcloud.sample2.ui.device.cricket

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.DialogCricketBinding
import com.topstep.fitcloud.sample2.utils.PARCEL_ARGS
import com.topstep.fitcloud.sample2.utils.getParcelableCompat
import java.util.*
import kotlin.math.roundToInt

class CricketDialogFragment : AppCompatDialogFragment() {

    private var _viewBind: DialogCricketBinding? = null
    private val viewBind get() = _viewBind!!
    private var edit = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogCricketBinding.inflate(LayoutInflater.from(context))
        var info = arguments?.getParcelableCompat<CricketInfo>(PARCEL_ARGS)
        edit = info != null
        if (info == null) {
            info = CricketInfo(
                matchId = 1,
                matchName = "test match",
                matchTime = System.currentTimeMillis(),
                team1Id = 1,
                team2Id = 2
            )
        }

        when (info.state) {
            CricketInfo.State.UPCOMING -> viewBind.rgState.check(R.id.rb_upcoming)
            CricketInfo.State.LIVE -> viewBind.rgState.check(R.id.rb_live)
            CricketInfo.State.RESULT -> viewBind.rgState.check(R.id.rb_result)
        }

        viewBind.editMatchId.setText(info.matchId.toString())
        viewBind.editMatchName.setText(info.matchName)
        viewBind.editMatchTime.setText(CricketFragment.timeFormat.format(Date(info.matchTime)))
        viewBind.editTeam1Id.setText(info.team1Id.toString())
        viewBind.editTeam2Id.setText(info.team2Id.toString())

        viewBind.editTeam1Data.setText(
            "${info.team1Runs},${info.team1Wickets},${info.team1Overs},${info.team1Balls}"
        )
        viewBind.editTeam2Data.setText(
            "${info.team2Runs},${info.team2Wickets},${info.team2Overs},${info.team2Balls}"
        )

        viewBind.editBatsman1Name.setText(info.batsman1Name)
        viewBind.editBatsman1Data.setText(
            "${info.batsman1Runs},${info.batsman1Balls}"
        )

        viewBind.editBatsman2Name.setText(info.batsman2Name)
        viewBind.editBatsman2Data.setText(
            "${info.batsman2Runs},${info.batsman2Balls}"
        )

        viewBind.editBowlerName.setText(info.bowlerName)
        viewBind.editBowlerData.setText(
            "${info.bowlerRuns},${info.bowlerWickets},${info.bowlerOvers},${info.bowlerBalls}"
        )

        when (info.innings) {
            2 -> viewBind.rgInnings.check(R.id.rb_second_inning)
            else -> viewBind.rgInnings.check(R.id.rb_first_inning)
        }

        viewBind.editCrr.setText(info.crr.toString())
        viewBind.editRrr.setText(info.rrr.toString())

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(viewBind.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val info = getResult()
                if (edit) {
                    (parentFragment as? Listener)?.onDialogEdit(
                        info,
                        requireArguments().getInt(EXTRA_POSITION)
                    )
                } else {
                    (parentFragment as? Listener)?.onDialogAdd(info)
                }
            }
        if (edit) {
            builder.setNeutralButton("Delete") { _, _ ->
                (parentFragment as? Listener)?.onDialogDelete(
                    requireArguments().getInt(EXTRA_POSITION)
                )
            }
        }
        return builder.create()
    }

    private fun getResult(): CricketInfo {
        val info = CricketInfo(
            matchId = viewBind.editMatchId.getTrimText().toIntSafe().toLong(),
            matchName = viewBind.editMatchName.getTrimText(),
            matchTime = try {
                CricketFragment.timeFormat.parse(viewBind.editMatchTime.getTrimText()).time
            } catch (e: Exception) {
                System.currentTimeMillis()
            },
            team1Id = viewBind.editTeam1Id.getTrimText().toIntSafe(),
            team2Id = viewBind.editTeam2Id.getTrimText().toIntSafe()
        )

        info.state = when (viewBind.rgState.checkedRadioButtonId) {
            R.id.rb_upcoming -> CricketInfo.State.UPCOMING
            R.id.rb_live -> CricketInfo.State.LIVE
            else -> CricketInfo.State.RESULT
        }

        val team1Data = viewBind.editTeam1Data.getTrimText().split(",")
        info.setTeam1Data(
            team1Data.getOrNull(0).toIntSafe(),
            team1Data.getOrNull(1).toIntSafe(),
            team1Data.getOrNull(2).toIntSafe(),
            team1Data.getOrNull(3).toIntSafe(),
        )

        val team2Data = viewBind.editTeam2Data.getTrimText().split(",")
        info.setTeam2Data(
            team2Data.getOrNull(0).toIntSafe(),
            team2Data.getOrNull(1).toIntSafe(),
            team2Data.getOrNull(2).toIntSafe(),
            team2Data.getOrNull(3).toIntSafe(),
        )

        val batsman1Data = viewBind.editBatsman1Data.getTrimText().split(",")
        info.setBatsman1Data(
            viewBind.editBatsman1Name.getTrimText(),
            batsman1Data.getOrNull(0).toIntSafe(),
            batsman1Data.getOrNull(1).toIntSafe(),
        )

        val batsman2Data = viewBind.editBatsman2Data.getTrimText().split(",")
        info.setBatsman2Data(
            viewBind.editBatsman2Name.getTrimText(),
            batsman2Data.getOrNull(0).toIntSafe(),
            batsman2Data.getOrNull(1).toIntSafe(),
        )

        val bowlerData = viewBind.editBowlerData.getTrimText().split(",")
        info.setBowlerData(
            viewBind.editBowlerName.getTrimText(),
            bowlerData.getOrNull(2).toIntSafe(),
            bowlerData.getOrNull(3).toIntSafe(),
            bowlerData.getOrNull(0).toIntSafe(),
            bowlerData.getOrNull(1).toIntSafe(),
        )

        info.innings = when (viewBind.rgInnings.checkedRadioButtonId) {
            R.id.rb_second_inning -> 2
            else -> 1
        }

        info.crr = viewBind.editCrr.getTrimText().toFloatOrNull() ?: 0f
        info.rrr = viewBind.editRrr.getTrimText().toFloatOrNull() ?: 0f

        return info
    }

    private fun EditText.getTrimText(): String {
        return this.text.toString().trim()
    }

    private fun String?.toIntSafe(): Int {
        if (this.isNullOrEmpty()) return 0
        return try {
            this.toInt()
        } catch (e1: Exception) {
            try {
                this.toFloat().roundToInt()
            } catch (e2: Exception) {
                0
            }
        }
    }

    interface Listener {
        fun onDialogAdd(info: CricketInfo)
        fun onDialogEdit(info: CricketInfo, position: Int)
        fun onDialogDelete(position: Int)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

    companion object {
        private const val EXTRA_POSITION = "position"
        fun newInstance(info: CricketInfo? = null, position: Int = 0): CricketDialogFragment {
            val fragment = CricketDialogFragment()
            if (info != null) {
                fragment.arguments = Bundle().apply {
                    putParcelable(PARCEL_ARGS, info)
                    putInt(EXTRA_POSITION, position)
                }
            }
            return fragment
        }
    }

}