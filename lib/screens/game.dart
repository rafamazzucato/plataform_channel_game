import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:plataform_channel_game/models/creator.dart';
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
              createGame();
            }),
      );

  createGame() {}

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
}
