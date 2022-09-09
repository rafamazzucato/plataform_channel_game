package com.example.plataform_channel_game

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
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
import java.util.Arrays

class MainActivity: FlutterActivity() {

    //ATENÇÃOOOOO porque o channel é do canal entre o nativo e o dart
    private val CHANNEL_NATIVE_DART = "game/exchange"

    //a instância do PubNub, que é nosso pubsub com a nuvem
    private var pubnub: PubNub? = null
    private var channel_pubnub: String? = null

    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler(Looper.getMainLooper())

        //vamos nos ligar ao projeto do pubnub, na nuvem
        val pnConfiguration = PNConfiguration("myUniqueUUID")
        pnConfiguration.subscribeKey = "sub-c-f9924aed-0246-4d7c-8187-38fd7dd1a018"
        pnConfiguration.publishKey = "pub-c-405eaa0c-628b-43a8-94ed-24af214ab398"
        pubnub = PubNub(pnConfiguration)

        pubnub?.let {
            it.addListener(object : SubscribeCallback() {
                override fun status(pubnub: PubNub, status: PNStatus) { }

                override fun message(pubnub: PubNub, message: PNMessageResult) {
                    //nós estamos pegando o jsonObject tap da mensagem oriunda do pubnub
                    //que vou enviado pelo nosso adversário
                    var receivedMessageObject: JsonElement? = null
                    var actionReceived = "sendAction"
                    if (message.message.asJsonObject["tap"] != null)
                        receivedMessageObject = message.message.asJsonObject["tap"]
                    else {
                        receivedMessageObject = message.message.asJsonObject["message"]
                        actionReceived = "chat"
                    }

                    Log.e("pubnub", "Received message content: $receivedMessageObject")

                    // por imposição da plataforma android nativa, algumas atividades
                    //que podem, demandar tempo não podem ser executadas na Main Thread.
                    //Por isso o usso do handler.
                    handler?.let {
                        //fazendo a coumincação PLatfrom Channel do NATIVO para o DART
                        it.post {
                            flutterEngine?.dartExecutor?.binaryMessenger?.let{
                                MethodChannel(it, CHANNEL_NATIVE_DART)
                                    .invokeMethod(
                                        actionReceived,
                                        "${receivedMessageObject.asString}"
                                    );
                            }

                        }
                    }
                }


                override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) {}
                override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}
                override fun uuid(pubnub: PubNub, pnUUIDMetadataResult: PNUUIDMetadataResult) {}
                override fun channel(pubnub: PubNub, pnChannelMetadataResult: PNChannelMetadataResult) {}
                override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}
                override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {}
                override fun file(pubnub: PubNub, pnFileEventResult: PNFileEventResult) {}
            })
        }

    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        //esse trecho de código é pro NATIVO responder a chamadas do DART
        flutterEngine.dartExecutor.binaryMessenger?.let{
            MethodChannel(it, CHANNEL_NATIVE_DART).setMethodCallHandler {
                    call, result ->
                if (call.method == "sendAction") {
                    pubnub!!.publish()
                        .message(call.arguments)
                        .channel(channel_pubnub)
                        .async { result, status ->
                            Log.e("pubnub", "teve erro? ${status.isError}")
                        }
                    result.success(true)
                } else if (call.method == "subscribe") {
                    subscribeChannel(call.argument("channel"))
                    result.success(true)
                } else if (call.method == "chat") {
                    pubnub!!.publish()
                        .message(call.arguments)
                        .channel(channel_pubnub)
                        .async { result, status ->
                            Log.e("pubnub", "teve erro? ${status.isError}")
                        }
                    result.success(true)
                } else {
                    result.notImplemented()
                }
            }
        }

    }

    fun subscribeChannel(channelName: String?){
        channel_pubnub = channelName
        channelName?.let {
            pubnub?.subscribe()?.channels(Arrays.asList(channelName))?.execute();
        }
    }

}
