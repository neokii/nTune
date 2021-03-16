package com.neokii.ntune

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_ssh_setting.*
import java.io.InputStreamReader


class SshKeySettingActivity: AppCompatActivity() {

    companion object {
        const val PREF_PRIVATE_KEY = "pref_ssh_private_key"
        const val PREF_PUBLIC_KEY = "pref_ssh_public_key"
        const val PREF_PASSWORD_KEY = "pref_ssh_password"
        const val PICK_SSH_PRIVATE = 0
        const val PICK_SSH_PUBLIC = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ssh_setting)

        btnPrivateFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"

            startActivityForResult(intent, PICK_SSH_PRIVATE)
        }

        btnPublicFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"

            startActivityForResult(intent, PICK_SSH_PUBLIC)
        }

        btnClear.setOnClickListener {

            AlertDialog.Builder(this@SshKeySettingActivity)
                .setMessage(R.string.confirm_clear_ssh_key)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->

                    editPassword.setText("")
                    editPrivateKey.setText("")
                    editPublicKey.setText("")
                }
                .show()
        }

        editPassword.addTextChangedListener(passwordListener)
        editPrivateKey.addTextChangedListener(privateListener)
        editPublicKey.addTextChangedListener(publicListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        editPassword.removeTextChangedListener(passwordListener)
        editPrivateKey.removeTextChangedListener(privateListener)
        editPublicKey.removeTextChangedListener(publicListener)
    }

    override fun onResume() {
        super.onResume()
        editPassword.setText(SettingUtil.getString(this, PREF_PASSWORD_KEY, ""))
        editPrivateKey.setText(SettingUtil.getString(this, PREF_PRIVATE_KEY, ""))
        editPublicKey.setText(SettingUtil.getString(this, PREF_PUBLIC_KEY, ""))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode == RESULT_OK) {
            var uri: Uri? = null
            if (resultData != null) {
                uri = resultData.data

                if (uri != null) {

                    try {
                        val text = InputStreamReader(contentResolver.openInputStream(uri)).readText()

                        if(requestCode == PICK_SSH_PRIVATE)
                            editPrivateKey.setText(text)
                        else if(requestCode == PICK_SSH_PUBLIC)
                            editPublicKey.setText(text)

                    }
                    catch (e: Exception){

                        Snackbar.make(
                            findViewById(android.R.id.content),
                            e.localizedMessage,
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }
        }
    }

    private val passwordListener = object: TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            SettingUtil.setString(MyApp.getContext(), PREF_PASSWORD_KEY, s.toString())
        }
    }

    private val privateListener = object: TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            SettingUtil.setString(MyApp.getContext(), PREF_PRIVATE_KEY, s.toString())
        }
    }

    private val publicListener = object: TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            SettingUtil.setString(MyApp.getContext(), PREF_PUBLIC_KEY, s.toString())
        }
    }


}