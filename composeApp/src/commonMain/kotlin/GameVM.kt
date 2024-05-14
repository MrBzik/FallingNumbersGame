import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Stack

class GameVM : ViewModel() {

    private var gameState = GameState.PLAYING
    private var maxNumber = 32
    private var lastFrame : Long? = null

    private var gameSpeed = 0.5f

    private val gameBoard : Array<Array<NumBox?>> = Array(BOARD_HEIGHT){ Array(BOARD_WIDTH) { null } }

    private val _board : MutableStateFlow<Array<Array<NumBox?>>> = MutableStateFlow(
        gameBoard
    )
    val board = _board.asStateFlow()

    private val _fallingBoxes : MutableStateFlow<List<FallingBox>> = MutableStateFlow(emptyList())
    val fallingBoxes = _fallingBoxes.asStateFlow()

    private val _mergingBoxes : MutableStateFlow<List<MergingBox>> = MutableStateFlow(emptyList())
    val mergingBoxes = _mergingBoxes.asStateFlow()

    private val checkMatchesStack = Stack<BoxIdx>()

    private val _curNumBox : MutableStateFlow<FallingBox> = MutableStateFlow(getRandomBox())
    val curNumBox = _curNumBox.asStateFlow()

    private val _mergeTargetBox : MutableStateFlow<MergingTargetBox?> = MutableStateFlow(null)
    val mergeTargetBox = _mergeTargetBox.asStateFlow()

    fun onNewFrame(frameMills: Long){

        lastFrame?.let {  last ->

            val delta = (frameMills - last).toFloat() / 1000

            when(gameState){
                GameState.PLAYING -> {
                    _curNumBox.update {
                        val yPos = it.y + delta * gameSpeed
                        val isDropped = isBoxDroppedOnBoard(it, yPos)
                        if(isDropped){
                            gameState = GameState.CHECKING
                            it.copy(numBox = NumBox.NUM_BLANK)
                        }
                        else {
                            it.copy(y = yPos)
                        }
                    }
                }
                GameState.FALLING -> {
                    val update = mutableListOf<FallingBox>()
                    _fallingBoxes.value.forEach {
                        val yPos = it.y + delta * FALL_SPEED
                        val isDropped = isBoxDroppedOnBoard(it, yPos)
                        if(!isDropped) update.add(it.copy(y = yPos))
                    }
                    if(update.isEmpty()) gameState = GameState.CHECKING

                    _fallingBoxes.value = update
                }

                GameState.MERGING -> {
                    onMergeBoxesFrame(delta)
                }

                GameState.PAUSED -> {

                }

                GameState.CHECKING -> {
                    checkMatches()
                }
            }
        }


        lastFrame = frameMills
    }


    private fun isBoxDroppedOnBoard(box : FallingBox, yPos : Float) : Boolean {
        val xIdx = box.x.toInt()
        val targetY = box.targetY
        if(yPos >= targetY){
            gameBoard[targetY][xIdx] = box.numBox
            _board.value = getBoardCopy()
            checkMatchesStack.push(BoxIdx(row = targetY, col = xIdx))
            return true
        }
        return false
    }

    private fun getBoardCopy() : Array<Array<NumBox?>>{

        val clone = Array<Array<NumBox?>>(BOARD_HEIGHT){
            Array(BOARD_WIDTH){
                null
            }
        }

        gameBoard.forEachIndexed { y, numBoxes ->
            clone[y] = gameBoard[y].clone()
        }

        return clone
    }


    private fun onMergeBoxesFrame(delta: Float) {

        _mergingBoxes.update { boxes ->

            var isMerged = false

            val update = mutableListOf<MergingBox>()

            boxes.forEach { box ->
                var x = box.x
                var y = box.y
                when(box.vector){
                    Vector.UP -> {
                        y = (y - (delta * ANIM_SPEED)).coerceAtLeast(box.targetY)
                    }
                    Vector.LEFT -> {
                        x = (x - (delta * ANIM_SPEED)).coerceAtLeast(box.targetX)
                    }
                    Vector.RIGHT -> {
                        x = (x + (delta * ANIM_SPEED)).coerceAtMost(box.targetX)
                    }
                }
                isMerged = x == box.targetX && y == box.targetY
                update.add(box.copy(x = x, y = y))
            }

            if(isMerged){
                passMergedBoxForward()
                gameState = GameState.FALLING
                emptyList()
            }
            else {
                update
            }
        }
    }


    private fun passMergedBoxForward(){

        println("end: ${System.currentTimeMillis()}")

        _mergeTargetBox.update {

            it?.let { target ->

                _fallingBoxes.update { fallingBoxes ->

                    fallingBoxes + FallingBox(
                        numBox = target.targetBox,
                        x = target.x,
                        y = target.y,
                        targetY = getDepth(target.x.toInt())
                    )
                }
            }

            null
        }
    }

    private fun getDepth(col : Int) : Int {

        var depth = BOARD_HEIGHT - 1

        for(row in BOARD_HEIGHT - 1 downTo 0){

            if(gameBoard[row][col] != null){
                depth --
            } else {
                break
            }
        }
        return depth

    }


    private fun checkMatches(){

        if(checkMatchesStack.isEmpty()){
            _curNumBox.value = getRandomBox()
            gameState = GameState.PLAYING
            return
        }

        while (checkMatchesStack.isNotEmpty()){

            val check = checkMatchesStack.pop()

            val isMatchFound = onNumberDropped(check.row, check.col)

            if(isMatchFound) {
                println("str: ${System.currentTimeMillis()}")
                gameState = GameState.MERGING
                break
            }
        }
    }



    private fun onNumberDropped(y: Int, x: Int) : Boolean {

        val numBox = gameBoard[y][x] ?: return false
        val boxesToMerge = mutableListOf<MergingBox>()
        val fallingBoxes = mutableListOf<FallingBox>()
        var isMatchesFound = false

        fun isMatch(yIdx: Int, xIdx: Int, vector: Vector)  {
            gameBoard[yIdx][xIdx]?.let { match ->
                if(match.number == numBox.number){
                    isMatchesFound = true
                    boxesToMerge.add(
                        MergingBox(
                        numBox = match,
                        x = xIdx.toFloat(),
                        y = yIdx.toFloat(),
                        vector = vector,
                        targetX = x.toFloat(),
                        targetY = y.toFloat()
                    ))
                    gameBoard[yIdx][xIdx] = null

                    val startIdx = if(vector == Vector.UP) yIdx - 2 else yIdx - 1

                    for(i in startIdx downTo 0){

                        val numB = gameBoard[i][xIdx] ?: break

                        gameBoard[i][xIdx] = null

                        fallingBoxes.add(FallingBox(
                            numBox = numB,
                            x = xIdx.toFloat(),
                            y = i.toFloat(),
                            targetY = i + 1
                        ))
                    }
                }
            }
        }


        if(x > 0){
            isMatch(y, x - 1, Vector.RIGHT)
        }
        if(x < BOARD_WIDTH - 1){
            isMatch(y, x + 1, Vector.LEFT)
        }
        if(y < BOARD_HEIGHT - 1){
            isMatch(y + 1, x, Vector.UP)
        }

        if(isMatchesFound){

            gameBoard[y][x] = null

            sendMergingTargetBox(boxesToMerge)

            _fallingBoxes.value = fallingBoxes

            _mergingBoxes.value = boxesToMerge

            _board.value = getBoardCopy()

        }


        return isMatchesFound

    }


    private fun sendMergingTargetBox(matches: List<MergingBox>){

        val multiplier = matches.size
        var num = matches.first().numBox.number
        repeat(multiplier){
            num *= 2
        }
        val newNumBox = NumBox.entries.find {
            it.number == num
        } ?: return

        val targetBox = MergingTargetBox(
            startBox =  matches.first().numBox,
            x = matches.first().targetX,
            y = matches.first().targetY,
            targetBox = newNumBox
        )

        _mergeTargetBox.value = targetBox

    }


    fun onUserBoardInput(posX: Int, isTap : Boolean){

        if(gameState != GameState.PLAYING) return

        if(!isValidInput(posX)){
            // Notify User TODO
            return
        }

        val updatedBox = _curNumBox.value.copy(x = posX.toFloat(), targetY = getDepth(posX))

        if(isTap){
            gameState = GameState.FALLING
            _fallingBoxes.value = listOf(updatedBox)
            _curNumBox.update {
                it.copy(numBox = NumBox.NUM_BLANK)
            }
        } else {
            _curNumBox.value = updatedBox
        }
    }

    private fun isValidInput(posX: Int) : Boolean{

        if(posX < 0 || posX > BOARD_WIDTH - 1) return false

        val oldX = _curNumBox.value.x.toInt()

        if(posX != oldX){
            val yIdx = _curNumBox.value.y.toInt() + 1

            val range = if (posX > oldX) oldX + 1 ..posX
            else oldX - 1 downTo posX
            for(i in range) if(gameBoard[yIdx][i] != null) return false
        }

        return true
    }



    private fun getRandomBox() : FallingBox {

        val numBox = NumBox.entries.filter {
            it.number in 2..maxNumber
        }.random()

        val x = BOARD_WIDTH / 2
        val targetY = getDepth(x)

        return FallingBox(
            numBox = numBox,
            x = x.toFloat(),
            y = 0f,
            targetY = targetY
            )

    }
}
