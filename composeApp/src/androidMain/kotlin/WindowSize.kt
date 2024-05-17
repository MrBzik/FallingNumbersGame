
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

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


