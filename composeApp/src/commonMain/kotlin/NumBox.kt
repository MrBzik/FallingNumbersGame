import androidx.compose.ui.graphics.Color


enum class NumBox(val number: Int, val color: Color, val border : Color, val label: String){

    NUM_BLANK(0, Color.Transparent, Color.Transparent, ""),
    NUM_2(2, Color(0xffef66e2), bronze, "2"),
    NUM_4(4, Color(0xff5ef15a), bronze, "4"),
    NUM_8(8, Color(0xff46ffea), bronze, "8"),
    NUM_16(16, Color(0xff415ff5), bronze, "16"),
    NUM_32(32, Color(0xffe83944), bronze, "32"),
    NUM_64(64, Color(0xffecd02b), bronze, "64"),
    NUM_128(128, Color(0xff757575), bronze, "128"),
    NUM_256(256, Color(0xffa679ea), bronze, "256"),
    NUM_512(512, Color(0xff3304d3), bronze, "512"),
    NUM_1024(1024, Color(0xffd9660a), platinum, "1K"),

}


val bronze = Color(0xffcc6201)
val platinum = Color(0xff939393)
val gold = Color(0xffecd02b)
val sapphire = Color(0xff3719f5)