import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo


@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getWindowHeight() : Int {
    return LocalWindowInfo.current.containerSize.height
}