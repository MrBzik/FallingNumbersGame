package data

import entities.NumBox
import kotlinx.serialization.Serializable

@Serializable
data class SaveFile(
    val board: Array<Array<NumBox?>>,
) {

}
