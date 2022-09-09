package com.example.plataform_channel_game

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import com.google.gson.JsonElement
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.lang.reflect.Array
import java.util.*

class MainActivity: FlutterActivity() {

    private val CHANNEL_NATIVE_DART = "game/exchange"

    private var pubNub: PubNub? = null
    private var channel_pubnub: String? = null

    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        handler = Handler(Looper.getMainLooper())

        val pnConfiguration = PNConfiguration("MySuperSecretKey")
        pnConfiguration.subscribeKey = "sub-c-f9924aed-0246-4d7c-8187-38fd7dd1a018"
        pnConfiguration.publishKey = "pub-c-405eaa0c-628b-43a8-94ed-24af214ab398"
        pubNub = PubNub(pnConfiguration)

        pubNub?.let {
            it.addListener(object: SubscribeCallback(){
                override fun status(pubnub: PubNub, pnStatus: PNStatus) {}

                override fun message(pubnub: PubNub, message: PNMessageResult) {
                    var receivedMessage: JsonElement? = null
                    var actionReceived = "sendAction"
                    if(message.message.asJsonObject["tap"] != null){
                        receivedMessage = message.message.asJsonObject["tap"]
                    }else {
                        receivedMessage = message.message.asJsonObject["message"]
                        actionReceived = "chat"
                    }

                    Log.e("pubnub", "Received Message content: $receivedMessage")

                    handler?.let {
                        it.post{
                            flutterEngine?.dartExecutor?.binaryMessenger?.let {
                                MethodChannel(it, CHANNEL_NATIVE_DART)
                                    .invokeMethod(actionReceived, "${receivedMessage.asString}")
                            }
                        }
                    }
                }

                override fun presence(
                    pubnub: PubNub,
                    pnPresenceEventResult: PNPresenceEventResult
                ) {}

                override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}

                override fun uuid(pubnub: PubNub, pnUUIDMetadataResult: PNUUIDMetadataResult) {}

                override fun channel(
                    pubnub: PubNub,
                    pnChannelMetadataResult: PNChannelMetadataResult
                ) {}

                override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}

                override fun messageAction(
                    pubnub: PubNub,
                    pnMessageActionResult: PNMessageActionResult
                ) {}

                override fun file(pubnub: PubNub, pnFileEventResult: PNFileEventResult) {}
            })
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        flutterEngine?.dartExecutor?.binaryMessenger?.let {
            MethodChannel(it, CHANNEL_NATIVE_DART)
                .setMethodCallHandler{ call, result ->
                  if(call.method == "sendAction"){
                      pubNub!!.publish().message(call.arguments).channel(channel_pubnub).async{
                              result, stats ->
                                Log.e("pubnub", "Ocorreu erro: ${stats.isError}`")}
                      result.success(true)
                  }else if(call.method == "subscribe"){
                      subscribeChannel(call.argument("channel"))
                      result.success(true)
                  }else if(call.method == "chat"){
                      pubNub!!.publish().message(call.arguments).channel(channel_pubnub).async{
                              result, stats ->
                          Log.e("pubnub", "Ocorreu erro: ${stats.isError}`")}
                      result.success(true)
                  }else{
                      result.notImplemented()
                  }
                }
        }
    }

    fun subscribeChannel(channelName : String?){
        channel_pubnub = channelName
        channelName.let {
            pubNub?.subscribe()?.channels(Arrays.asList(channelName))?.execute()
        }
    }

}
