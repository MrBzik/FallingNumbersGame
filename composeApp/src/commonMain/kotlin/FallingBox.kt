
data class FallingBox(val numBox: NumBox, val x: Float, val y: Float, val targetY : Int)

data class MergingBox(val numBox: NumBox, val x: Float, val y: Float, val vector: Vector, val targetX: Float, val targetY: Float){
}

enum class Vector {
    UP, LEFT, RIGHT, TARGET
}


