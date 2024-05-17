import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun getWindowHeight() : Int


@Composable
expect fun <T> StateFlow<T>.collectAsStateSafely() : State<T>