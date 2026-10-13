package com.subtrack.app.util

import android.content.Context
import android.net.Uri
import com.subtrack.app.data.Subscription
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    fun exportToJson(subscriptions: List<Subscription>): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("exported_at", System.currentTimeMillis())

        val arr = JSONArray()
        subscriptions.forEach { sub ->
            val obj = JSONObject().apply {
                put("name", sub.name)
                put("price", sub.price)
                put("currency", sub.currency)
                put("category", sub.category)
                put("cycle", sub.cycle)
                put("customCycleDays", sub.customCycleDays)
                put("firstPaymentDate", sub.firstPaymentDate)
                put("nextPaymentDate", sub.nextPaymentDate)
                put("hasTrial", sub.hasTrial)
                put("trialEndDate", sub.trialEndDate ?: JSONObject.NULL)
                put("priceAfterTrial", sub.priceAfterTrial ?: JSONObject.NULL)
                put("isCancelled", sub.isCancelled)
                put("cancelledDate", sub.cancelledDate ?: JSONObject.NULL)
                put("notes", sub.notes)
            }
            arr.put(obj)
        }
        root.put("subscriptions", arr)
        return root.toString(2)
    }

    fun parseJson(json: String): List<Subscription> {
        val root = JSONObject(json)
        val arr = root.getJSONArray("subscriptions")
        val result = mutableListOf<Subscription>()
        val now = System.currentTimeMillis()

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)

            // === ЧТЕНИЕ ДАТ С ПРОВЕРКОЙ ===
            val firstPayment = if (obj.has("firstPaymentDate"))
                obj.getLong("firstPaymentDate") else now

            val nextPayment = if (obj.has("nextPaymentDate"))
                obj.getLong("nextPaymentDate")
            else
                calculateNextPaymentDate(firstPayment, obj.optString("cycle", "MONTHLY"), 30)

            result.add(
                Subscription(
                    id = 0,
                    name = obj.optString("name", "Без названия"),
                    price = obj.optDouble("price", 0.0),
                    currency = obj.optString("currency", "₽"),
                    category = obj.optString("category", "Другое"),
                    cycle = obj.optString("cycle", "MONTHLY"),
                    customCycleDays = obj.optInt("customCycleDays", 30),
                    firstPaymentDate = firstPayment,
                    nextPaymentDate = nextPayment,
                    hasTrial = obj.optBoolean("hasTrial", false),
                    trialEndDate = if (obj.isNull("trialEndDate")) null else obj.optLong("trialEndDate"),
                    priceAfterTrial = if (obj.isNull("priceAfterTrial")) null else obj.optDouble("priceAfterTrial"),
                    isCancelled = obj.optBoolean("isCancelled", false),
                    cancelledDate = if (obj.isNull("cancelledDate")) null else obj.optLong("cancelledDate"),
                    notes = obj.optString("notes", "")
                )
            )
        }
        return result
    }

    fun writeToUri(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
        }
    }

    fun readFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: ""
    }
}
