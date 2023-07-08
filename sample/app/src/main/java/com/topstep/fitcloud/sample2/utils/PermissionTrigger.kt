package com.topstep.fitcloud.sample2.utils

import android.content.Context
import android.database.Cursor
import android.provider.Telephony

/**
 * Used to trigger permission dialog for certain permissions。
 * For example, Xiaomi does not pop up the dialog when obtaining SMS permission.
 * The permission dialog will only pop up when the SMS is actually read.
 */
object PermissionTrigger {

    fun readSmsTest(context: Context): Boolean {
        return try {
            val projection = arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.PERSON, Telephony.Sms.BODY)
            val cursor = context.contentResolver.query(Telephony.Sms.CONTENT_URI, projection, null, null, null)
            if (cursor != null) {
                cursor.use { readCursor(it) }
                true
            } else {
                false
            }
        } catch (e: Throwable) {
            false
        }
    }

    private fun readCursor(cursor: Cursor) {
        if (cursor.moveToFirst()) {
            when (cursor.getType(0)) {
                Cursor.FIELD_TYPE_BLOB, Cursor.FIELD_TYPE_NULL -> {}
                Cursor.FIELD_TYPE_INTEGER, Cursor.FIELD_TYPE_FLOAT, Cursor.FIELD_TYPE_STRING -> {
                    cursor.getString(0)
                }
                else -> {
                    cursor.getString(0)
                }
            }
        }
    }
}