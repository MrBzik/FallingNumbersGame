import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Stack

class GameVM : ViewModel() {

    private var gameState = GameState.PLAYING
    private var maxNumber = 32
    private var lastFrame : Long? = null
    private var maxDepth = IntArray(BOARD_WIDTH){
        BOARD_HEIGHT - 1
    }

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
            maxDepth[xIdx] = targetY - 1
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
                var isTarget = false
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
                    Vector.TARGET -> {
                        isTarget = true
                    }
                }
                if(isTarget){
                    update.add(box)
                } else {
                    isMerged = x == box.targetX && y == box.targetY
                    update.add(box.copy(x = x, y = y))
                }
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

        val multiplier = _mergingBoxes.value.size - 1
        _mergingBoxes.value.first().apply {
            var sum = numBox.number
            repeat(multiplier){
                sum*=2
            }
            NumBox.entries.find {
                it.number == sum
            }?.let { newNumber ->

                _fallingBoxes.update {
                    it + FallingBox(newNumber, targetX, targetY, maxDepth[targetX.toInt()])
                }
            }
        }
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
                gameState = GameState.MERGING
                break
            }
        }
    }



    private fun onNumberDropped(y: Int, x: Int) : Boolean {

        val numBox = gameBoard[y][x] ?: return false

        val boxesToMerge = mutableListOf<MergingBox>()
        val fallingBoxes = mutableListOf<FallingBox>()

        fun isMatch(yIdx: Int, xIdx: Int, vector: Vector) : Int {
            gameBoard[yIdx][xIdx]?.let { match ->
                if(match.number == numBox.number){
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
                    maxDepth[xIdx] = yIdx - 1


                    if(vector == Vector.LEFT || vector == Vector.RIGHT){

                        maxDepth[xIdx] ++

                        for(i in yIdx - 1 downTo 0){

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

                    return 1
                }
            }
            return 0
        }

        var matches = 0

        if(x > 0){
            matches += isMatch(y, x - 1, Vector.RIGHT)
        }
        if(x < BOARD_WIDTH - 1){
            matches += isMatch(y, x + 1, Vector.LEFT)
        }
        if(y < BOARD_HEIGHT - 1){
            matches += isMatch(y + 1, x, Vector.UP)
        }

        if(matches > 0){
            boxesToMerge.add(
                MergingBox(
                    numBox = numBox,
                    x = x.toFloat(),
                    y = y.toFloat(),
                    vector = Vector.TARGET,
                    targetX = x.toFloat(),
                    targetY = y.toFloat()
                )
            )

            _fallingBoxes.value = fallingBoxes

            _mergingBoxes.value = boxesToMerge

            gameBoard[y][x] = null
            maxDepth[x] +=1

            _board.value = getBoardCopy()

        }


        return matches > 0

    }


    fun onUserBoardInput(posX: Int, isTap : Boolean){

        if(gameState != GameState.PLAYING) return

        if(!isValidInput(posX)){
            // Notify User TODO
            return
        }

        val updatedBox = _curNumBox.value.copy(x = posX.toFloat(), targetY = maxDepth[posX])

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
            val curYPos = _curNumBox.value.y
            val range = if (posX > oldX) oldX + 1 ..posX
            else oldX - 1 downTo posX
            for(i in range) if(curYPos > maxDepth[i]) return false
        }

        return true
    }



    private fun getRandomBox() : FallingBox {

        val numBox = NumBox.entries.filter {
            it.number in 2..maxNumber
        }.random()

        val x = BOARD_WIDTH / 2
        val targetY = maxDepth[x]

        return FallingBox(
            numBox = numBox,
            x = x.toFloat(),
            y = 0f,
            targetY = targetY
            )

    }


}
