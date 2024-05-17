package entities

import entities.NumBox

data class MergingTargetBox(
    val startBox: NumBox, val x: Float, val y: Float, val targetBox : NumBox
)