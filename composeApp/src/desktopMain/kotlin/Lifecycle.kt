import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow

@Composable
actual fun <T> StateFlow<T>.collectAsStateSafely() : State<T> {
    return this.collectAsState()
}