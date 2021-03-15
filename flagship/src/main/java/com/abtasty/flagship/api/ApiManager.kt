package com.abtasty.flagship.api

import com.abtasty.flagship.database.DatabaseManager
import com.abtasty.flagship.main.Flagship
import com.abtasty.flagship.main.Flagship.Companion.VISITOR_ID
import com.abtasty.flagship.main.Flagship.Companion.apiKey
import com.abtasty.flagship.model.Campaign
import com.abtasty.flagship.utils.Logger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

internal class ApiManager {

    val DOMAIN = "https://decision.flagship.io/v2/"
    val CAMPAIGNS = "/campaigns"
    val EVENTS = "/events"
    val ARIANE = "https://ariane.abtasty.com"
    val ACTIVATION = "activate"
    val BUCKETING = "https://cdn.flagship.io/{id}/bucketing.json"
    val EXPOSE_KEYS_PARAM = "?exposeAllKeys=true"
    val CONTEXT_EVENT_PARAM = "&sendContextEvent=false"

    companion object {

        val IF_MODIFIED_SINCE = "If-Modified-Since"
        val X_API_KEY = "x-api-key"
        val LAST_MODIFIED = "last-modified"

        var cacheDir: File? = null
        var callTimeout = 2L
        var callTimeoutUnit : TimeUnit = TimeUnit.SECONDS

        private var instance: ApiManager = ApiManager()

        @Synchronized
        fun getInstance(): ApiManager {
            return instance
        }
    }

    private var client: OkHttpClient = OkHttpClient().newBuilder().build()


    private fun getEndPoint() : String {
        return DOMAIN
    }

    internal interface PostRequestInterface<B, I> {

        var instance: I

        fun build(): I
        fun withUrl(url: String): B
        fun withRequestId(requestId: Long): B
        fun withBodyParams(jsonObject: JSONObject): B
        fun withBodyParam(key: String, value: Any): B
        fun withBodyParams(params: HashMap<String, Any>): B
        fun withRequestIds(requestId: List<Long>): B
        fun withHeaderParam(key: String, value: String): B
        fun withTimeout(timeout: Long, timeUnit: TimeUnit): B
    }

    enum class METHOD { POST, GET }

    open class ApiRequest(private var method: METHOD = METHOD.POST) {

        internal open var url: String = ""
        internal open var jsonBody = JSONObject()
        internal var headers = HashMap<String, String>()
        internal var request: Request? = null
        internal var response: Response? = null
        internal var responseBody: String? = null
        internal var code = 0
        internal var callTimeout = 0L
        internal var callTimeoutUnit = TimeUnit.SECONDS

        internal var requestIds = mutableListOf<Long>()

        open fun build() {

            val builder = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
            for (h in headers)
                builder.addHeader(h.key, h.value)
//            apiKey?.let { key -> builder.addHeader(X_API_KEY, key) }
            if (method == METHOD.POST) {
                val body = jsonBody.toString()
                        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                builder.post(body)
            }
            request = builder.build()
        }

        open fun fire(async: Boolean) {
            try {
                build()
                request?.let {
                    logRequest(async)
                    if (!async) {
                        try {
                            val response = getHttpClient().newCall(it).execute()
                            onResponse(response)
                        } catch (e: Exception) {
                            onFailure(null, e.message ?: "")
                        }
                    } else
                        instance.client.newCall(it).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                onFailure(null, e.message ?: "")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                onResponse(response)
                            }
                        })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getHttpClient(): OkHttpClient {

            return if (callTimeout > 0) {
                getInstance().client.newBuilder()
                        .callTimeout(callTimeout, callTimeoutUnit)
                        .build()
            } else
                getInstance().client
        }

        open fun onFailure(response: Response?, message: String = "") {
            Logger.e(
                    if (method == METHOD.POST) Logger.TAG.POST else Logger.TAG.GET, "[Response${getIdToString()}][FAIL]" +
                    when (true) {
                        response != null -> "[${response.code}][${responseBody}]"
                        message.isNotEmpty() -> "[$message]"
                        headers.isNotEmpty() -> "[headers=$headers]"
                        else -> ""
                    }
            )
        }

        open fun onSuccess() {
            parseResponse()
        }

        private fun onResponse(response: Response) {
            this.code = response.code
            this.response = response
            logResponse(response.code)
            this.responseBody = response.body?.string() ?: ""
            if (response.isSuccessful || code == 304) {
                onSuccess()
            } else
                onFailure(response)
        }

        open fun parseResponse(): Boolean {
            return response?.isSuccessful ?: false
        }

        protected fun getIdToString(): String {
            return if (requestIds.size > 0) ":$requestIds" else ""
        }

        protected open fun logRequest(async: Boolean) {
            Logger.v(
                    if (method == METHOD.POST) Logger.TAG.POST else Logger.TAG.GET,
                    "[Request${getIdToString()}][async=$async] " + request?.url + " " + request?.headers + " " + jsonBody
            )
        }

        protected open fun logResponse(code: Int) {
            when (code) {
                in 200..299, 304 -> Logger.v(
                        if (method == METHOD.POST) Logger.TAG.POST else Logger.TAG.GET,
                        "[Response${getIdToString()}][$code] " + request?.url + " " + jsonBody
                )
                else -> Logger.e(
                        if (method == METHOD.POST) Logger.TAG.POST else Logger.TAG.GET,
                        "[FAIL][Response${getIdToString()}][$code] " + request?.url + " " + jsonBody
                )
            }
        }
    }

    open class PostRequestBuilder<B, I : ApiRequest> : PostRequestInterface<B, I> {

        override var instance = ApiRequest() as I

        override fun withUrl(url: String): B {
            instance.url = url
            return this as B
        }

        override fun withBodyParams(params: HashMap<String, Any>): B {
            for (k in params) {
                instance.jsonBody.put(k.key, k.value)
            }
            return this as B
        }

        override fun withBodyParams(jsonObject: JSONObject): B {
            for (k in jsonObject.keys()) {
                instance.jsonBody.put(k, jsonObject.get(k))
            }
            return this as B
        }

        override fun withBodyParam(key: String, value: Any): B {
            instance.jsonBody.put(key, value)
            return this as B
        }

        override fun withRequestId(requestId: Long): B {
            instance.requestIds.add(requestId)
            return this as B
        }

        override fun withRequestIds(requestId: List<Long>): B {
            instance.requestIds.addAll(requestId)
            return this as B
        }

        override fun withHeaderParam(key: String, value: String): B {
            if (key.isNotEmpty() && value.isNotEmpty())
                instance.headers[key] = value
            return this as B
        }

        override fun withTimeout(timeout: Long, timeUnit: TimeUnit): B {
            if (timeout > 0) {
                instance.callTimeout = timeout
                instance.callTimeoutUnit = timeUnit
            }
            return this as B
        }

        override fun build(): I {
            return instance
        }
    }

    internal class BaseRequest() : ApiRequest() {
    }

    internal class BaseRequestBuilder() :
            PostRequestBuilder<BaseRequestBuilder, BaseRequest>() {
        override var instance = BaseRequest()
    }

    internal class CampaignRequest(var campaignId: String = "") : ApiRequest() {

        override fun onSuccess() {
            parseResponse()
        }

        override fun parseResponse(): Boolean {
            try {
                val body = responseBody
                if (body != null && body.isNotEmpty()) {
                    val jsonResponse = JSONObject(body)
                    if (campaignId.isEmpty()) {
                        Flagship.panicMode = jsonResponse.optBoolean("panic", false)
                        Flagship.modifications.clear()
                        val array = jsonResponse.getJSONArray("campaigns")
                        for (i in 0 until array.length()) {
                            val mods = Campaign.parse(array.getJSONObject(i))!!.getModifications(false)
                            Flagship.updateModifications(mods)
                        }
                    } else {
                        Flagship.updateModifications(
                                Campaign.parse(jsonResponse)!!.getModifications(
                                        false
                                )
                        )
                    }
                }
                DatabaseManager.getInstance().updateModifications()
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }
    }

    internal class CampaignRequestBuilder :
            PostRequestBuilder<CampaignRequestBuilder, CampaignRequest>() {

        override var instance = CampaignRequest()

        fun withCampaignId(campaignId: String): CampaignRequestBuilder {
            instance.campaignId = campaignId
            return this
        }
    }

    internal fun sendCampaignRequest(hashMap: HashMap<String, Any> = HashMap()) {

        try {
            val jsonBody = JSONObject()
            val context = getCleanContext(hashMap)
            jsonBody.put(VISITOR_ID, Flagship.visitorId)
            jsonBody.put("context", context)
            jsonBody.put("trigger_hit", false)
            apiKey?.let { key ->
                CampaignRequestBuilder()
                    .withUrl(getEndPoint() + Flagship.clientId + CAMPAIGNS + "/" + EXPOSE_KEYS_PARAM + CONTEXT_EVENT_PARAM)
                    .withBodyParams(jsonBody)
                    .withHeaderParam(X_API_KEY, key)
                    .withTimeout(callTimeout, callTimeoutUnit)
                    .build()
                    .fire(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun sendContextRequest(hashMap: HashMap<String, Any> = HashMap()) {
        try {
            val jsonBody = JSONObject()
            val context=  getCleanContext(hashMap)
            jsonBody.put(VISITOR_ID, Flagship.visitorId)
            jsonBody.put("type", "CONTEXT")
            jsonBody.put("data", context)
            BaseRequestBuilder()
                    .withUrl(getEndPoint() + Flagship.clientId + EVENTS)
                    .withBodyParams(jsonBody)
                    .build()
                    .fire(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getCleanContext(hashMap: HashMap<String, Any> = HashMap()) : JSONObject {
        val context = JSONObject()
        for (p in hashMap) {
            if (!p.key.startsWith("fs_"))
                context.put(p.key, p.value)
        }
        return context
    }

    internal class BucketingRequest : ApiRequest(METHOD.GET) {

        var campaignsJson: JSONArray? = null

        override fun onSuccess() {
            parseResponse()
        }

        override fun onFailure(response: Response?, message: String) {
            super.onFailure(response, message)
            DatabaseManager.getInstance().getBucket()?.let { campaignsJson = JSONArray(it) }
        }

        override fun parseResponse(): Boolean {
            try {

//                Flagship.useVisitorConsolidation = jsonData.optBoolean("visitorConsolidation")
                val headers = response?.headers
                val date = headers!![LAST_MODIFIED] ?: ""
                if (code in 200..299) {
                    val jsonData = JSONObject(responseBody)
                    Flagship.panicMode = jsonData.optBoolean("panic", false)
                    val campaignsArr = jsonData.optJSONArray("campaigns")
                    campaignsJson = campaignsArr ?: JSONArray()
                    DatabaseManager.getInstance().insertBucket(campaignsJson.toString(), date)
                    return true
                } else if (code == 304) {
                    Logger.v(Logger.TAG.BUCKETING, "[304] Not modified, load bucketing file from local")
                    DatabaseManager.getInstance().getBucket()?.let { campaignsJson = JSONArray(it) }
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            DatabaseManager.getInstance().getBucket()?.let { campaignsJson = JSONArray(it) }
            return false
        }
    }

    internal class BucketingRequestBuilder :
            PostRequestBuilder<BucketingRequestBuilder, BucketingRequest>() {
        override var instance = BucketingRequest()
    }

    internal fun sendBucketingRequest(): JSONArray? {

        return try {
            val request = BucketingRequestBuilder()
                    .withUrl(BUCKETING.replace("{id}", Flagship.clientId!!))
                    .withHeaderParam(ApiManager.IF_MODIFIED_SINCE, DatabaseManager.getInstance().getBucketLastModified()
                            ?: "")
                    .build()
            request.fire(false)
            request.campaignsJson
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    internal fun sendActivationRequest(variationGroupId: String, variationId: String) {

        val activation = Hit.Activation(variationGroupId, variationId)
        Hit.HitRequestBuilder(false)
                .withHit(activation)
                .withUrl(getEndPoint() + ACTIVATION)
                .build()
                .fire(true)
    }

    internal fun <T> sendHitTracking(hit: HitBuilder<T>) {
        Hit.HitRequestBuilder()
                .withHit(hit)
                .build()
                .fire(true)
    }


    internal fun fireOfflineHits() {
        DatabaseManager.getInstance().getNonSentHits().let { hits ->
            if (hits.isNotEmpty()) {
                try {
                    DatabaseManager.getInstance().updateHitStatus(hits.map { h -> h.id!! }, 1)
                    sendHitTracking(
                            Hit.Batch(
                                    Flagship.visitorId,
                                    hits
                            )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        DatabaseManager.getInstance().getNonSentActivations().let { hits ->
            if (hits.isNotEmpty()) {
                for (h in hits) {
                    try {
                        Hit.HitRequestBuilder(false)
                                .withBodyParams(JSONObject(h.content))
                                .withRequestId(h.id!!)
                                .withUrl(getEndPoint() + ACTIVATION)
                                .build()
                                .fire(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}