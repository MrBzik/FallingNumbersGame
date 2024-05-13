import androidx.compose.ui.graphics.Color


enum class NumBox(val number: Int, val color: Color){

    NUM_BLANK(0, Color.Transparent),
    NUM_2(2, Color(0xffef66e2)),
    NUM_4(4, Color(0xff5ef15a)),
    NUM_8(8, Color(0xff46ffea)),
    NUM_16(16, Color(0xff415ff5)),
    NUM_32(32, Color(0xffe83944)),
    NUM_64(64, Color(0xffecd02b)),
    NUM_128(128, Color(0xff757575)),
    NUM_256(256, Color(0xffa679ea)),
    NUM_512(512, Color(0xff3304d3)),
    NUM_1024(1024, Color(0xffd9660a)),

}
