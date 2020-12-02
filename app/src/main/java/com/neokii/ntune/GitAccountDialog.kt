package com.neokii.ntune

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.neokii.ntune.databinding.GitAccountDialogBinding

class GitAccountDialog : DialogFragment() {

    lateinit var binding: GitAccountDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GitAccountDialogBinding.inflate(inflater, container, false)

        binding.editUrl.setText(SettingUtil.getString(activity, "git_url", ""))
        binding.editId.setText(SettingUtil.getString(activity, "git_account_id", ""))
        binding.editPassword.setText(SettingUtil.getString(activity, "git_account_password", ""))

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnOk.setOnClickListener {

            SettingUtil.setString(activity, "git_url", binding.editUrl.text.toString())
            SettingUtil.setString(activity, "git_account_id", binding.editId.text.toString())
            SettingUtil.setString(
                activity,
                "git_account_password",
                binding.editPassword.text.toString()
            )
            dismiss()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }
}