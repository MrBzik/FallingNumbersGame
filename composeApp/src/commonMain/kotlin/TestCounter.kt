import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TestCounter : ViewModel() {

    private val _counter = MutableStateFlow(0)
    val counter = _counter.asStateFlow()

    init {

        viewModelScope.launch {

            while (true){
                delay(1000)
                _counter.update {
                    it + 1
                }
            }
        }
    }
}