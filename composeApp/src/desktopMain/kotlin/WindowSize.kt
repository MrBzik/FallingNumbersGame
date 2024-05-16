import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo


@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getWindowHeight() : Int {
    val size = LocalWindowInfo.current.containerSize
    println("PASS IT?")
    return 10
}