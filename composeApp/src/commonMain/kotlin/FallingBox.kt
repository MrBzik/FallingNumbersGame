import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Immutable
data class FallingBox(val numBox: NumBox, val x: Float, val y: Float, val targetY : Int, val scale: Float = 1f)

data class MergingBox(val numBox: NumBox, val x: Float, val y: Float, val vector: Vector, val targetX: Float, val targetY: Float, val color : Color? = null ){
}

enum class Vector {
    UP, LEFT, RIGHT
}


