import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:plataform_channel_game/models/creator.dart';
import 'package:plataform_channel_game/models/message.dart';
import 'package:plataform_channel_game/utils/constants.dart';

class Game extends StatefulWidget {
  const Game({Key? key}) : super(key: key);

  @override
  State<Game> createState() => _GameState();
}

class _GameState extends State<Game> {
  static const platform = MethodChannel("game/exchange");

  bool? minhaVez;
  WrapperCreator? creator;

  @override
  void initState(){
    super.initState();
    //configurarPubNub();
  }

  void configurarPubNub(){
    platform.setMethodCallHandler((call) async {
      String action = call.method;
      String argumentos = call.arguments.toString();
      List<String> parts = argumentos.split('|');

      if(action == "sendAction"){
        ExchangeMessage message = ExchangeMessage(parts[0], int.parse(parts[1]), int.parse(parts[2]));
        if(message.user == (creator!.creator ? "p2" : 'p1')){
          setState(() {
            minhaVez = true;
            cells[message.x][message.y] = 2;
          });
          checkWinner();
        }
      }else if(parts[0] == (creator!.creator ? 'p2' : 'p1')){
        _showChat(parts[1]);
      }
    });
  }

  List<List<int>> cells = [
    [0, 0, 0],
    [0, 0, 0],
    [0, 0, 0],
  ];

  final textStyle75 = const TextStyle(fontSize: 75, color: Colors.white);

  final textStyle36 = const TextStyle(fontSize: 36, color: Colors.white);

  Widget buildButton(String label, bool owner) => SizedBox(
        width: ScreenUtil().setWidth(300),
        child: OutlinedButton(
            child: Padding(
                padding: const EdgeInsets.all(8.0),
                child: Text(label, style: textStyle36)),
            onPressed: () {
              createGame(owner);
            }),
      );

  Future<void> createGame(bool isCreator) async{
    TextEditingController controller = TextEditingController();
    return showDialog(
      context: context, 
      barrierDismissible: false,
      builder: (BuildContext context){
        return AlertDialog(
          title: Text("Qual o nome do jogo?"),
          content: TextField(
            controller: controller,
          ),
          actions: [
            ElevatedButton(
              child: Text("Jogar"),
              onPressed: (){
                  Navigator.of(context).pop();
                  _sendAction('subscribe', {'channel': controller.text}).then((value){
                    setState(() {
                      creator = WrapperCreator(isCreator, controller.text);
                      minhaVez = isCreator;
                    });
                  });
              }),
            ElevatedButton(child: const Text("Cancelar"),
            onPressed: (){
              Navigator.of(context).pop();
            })
          ],
        );
      });
  }

  Future<bool>_sendAction(String action, Map<String, dynamic> arguments) async {
    try{
      final resultado = await platform.invokeMethod(action, arguments);
      if(resultado){
        return true;
      }
    }catch(e){}
    return false;
  }

  checkWinner(){

  }

  _sendMessage() {}

  getCell(int x, int y) => InkWell(
        child: Container(
          padding: const EdgeInsets.all(8),
          color: Colors.lightBlueAccent,
          child: Center(
              child: Text(
                  cells[x][y] == 0
                      ? ""
                      : cells[x][y] == 1
                          ? "X"
                          : "0",
                  style: textStyle75)),
        ),
        onTap: () async {},
      );

  @override
  Widget build(BuildContext context) {
    ScreenUtil.init(context, designSize: const Size(700, 1400));

    return (Scaffold(
        body: SingleChildScrollView(
      child: Stack(children: [
        Column(
          children: [
            Row(
              children: [
                Container(
                    width: ScreenUtil().setWidth(550),
                    height: ScreenUtil().setWidth(550),
                    color: colorBackBlue1),
                Container(
                    width: ScreenUtil().setWidth(150),
                    height: ScreenUtil().setWidth(550),
                    color: colorBackBlue2)
              ],
            ),
            Container(
                width: ScreenUtil().setWidth(700),
                height: ScreenUtil().setWidth(850),
                color: colorBackBlue3)
          ],
        ),
        Container(
          height: ScreenUtil().setHeight(1400),
          child: Center(
              child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              creator == null
                  ? Row(children: [
                      buildButton("Criar", true),
                      SizedBox(width: 10),
                      buildButton("Entrar", false),
                    ], mainAxisSize: MainAxisSize.min)
                  : InkWell(
                      child: Text(minhaVez == true
                          ? "Sua vez!!"
                          : "Aguarde a minha vez"),
                      onLongPress: () {
                        _sendMessage();
                      },
                    ),
              GridView.count(
                shrinkWrap: true,
                padding: const EdgeInsets.all(20),
                crossAxisSpacing: 10,
                mainAxisSpacing: 10,
                crossAxisCount: 3,
                children: [
                  getCell(0, 0),
                  getCell(0, 1),
                  getCell(0, 2),
                  getCell(1, 0),
                  getCell(1, 1),
                  getCell(1, 2),
                  getCell(2, 0),
                  getCell(2, 1),
                  getCell(2, 2)
                ],
              )
            ],
          )),
        )
      ]),
    )));
  }

  _showChat(String part){

  }
}
