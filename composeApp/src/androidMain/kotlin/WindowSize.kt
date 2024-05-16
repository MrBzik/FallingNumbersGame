import android.view.WindowMetrics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

@Composable
actual fun getWindowHeight() : Int {

//    val context = LocalContext.current
//    val displayMetrics = context.resources.displayMetrics
//    val height = displayMetrics.heightPixels
//    val density = displayMetrics.density
//
//    return (height / density).toInt()

    return LocalConfiguration.current.screenHeightDp
}