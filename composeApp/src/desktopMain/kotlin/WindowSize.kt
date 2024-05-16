import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getWindowHeight() : Int {
    return LocalWindowInfo.current.containerSize.height
}