import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {

        val gameVM = GameVM()

        val curNum = gameVM.curNumBox.collectAsState()

        val fallingBoxes = gameVM.fallingBoxes.collectAsState()

        val mergingBoxes = gameVM.mergingBoxes.collectAsState()

        val board = gameVM.board.collectAsState()

        val mergeTargetBox = gameVM.mergeTargetBox.collectAsState()

        val density = LocalDensity.current.density

        var boardWidth : Dp = 0.dp

        LaunchedEffect(Unit){

            while (true){
                withFrameMillis {

                    gameVM.onNewFrame(it)

                }
            }
        }



        fun onUserInput(offset : Offset, isTap: Boolean){
            val x = offset.x / density
            val pos = x / (boardWidth / BOARD_WIDTH).value
            gameVM.onUserBoardInput(pos.toInt(), isTap)
        }


        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(BOARD_WIDTH / BOARD_HEIGHT.toFloat())
        ) {

            SideEffect {
                boardWidth = maxWidth
            }

            val rowWidth = maxWidth / BOARD_WIDTH

            Row (modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit){
                detectTapGestures(onTap = {
                    onUserInput(it, true)
                })
            }
                .pointerInput(Unit){
                    detectDragGestures { change, _ ->
                        onUserInput(change.position, false)
                    }
                }

            ) {

                repeat(BOARD_WIDTH){

                    val isEven = it % 2 == 0

                    Box (modifier = Modifier
                        .fillMaxHeight()
                        .width(rowWidth)
                        .background(color = if(isEven) Color.Gray else Color.LightGray)
                    )
                }
            }

            val count = remember { mutableIntStateOf(0) }

            SideEffect {
                println(count.value)
                count.value ++
            }


            DrawMergingBoxes(
                mergingBoxes = mergingBoxes,
                rowWidth = rowWidth
            )


            DrawCurNum(
                curNum = curNum,
                rowWidth = rowWidth
            )


            DrawFallingBoxes(
                fallingBoxes = fallingBoxes,
                rowWidth = rowWidth
            )

            DrawBoard(
                board = board,
                rowWidth = rowWidth
            )

            DrawMergeTargetBox(
                box = mergeTargetBox,
                rowWidth = rowWidth
            )
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DrawMergeTargetBox(
    box: State<MergingTargetBox?>,
    rowWidth: Dp
){

    box.value?.let { b ->

        val color = remember { Animatable(b.startBox.color) }

        val animSpeed = remember { 1000 / ANIM_SPEED / 2 }

        LaunchedEffect(Unit){
            color.animateTo(b.targetBox.color, animationSpec = tween(1000 / ANIM_SPEED))
        }

        var count by remember { mutableStateOf(b.startBox.number)}

        Box(modifier = Modifier.offset(
            x = (b.x * rowWidth.value).dp,
            y = (b.y * rowWidth.value).dp
        )
            .size(rowWidth)
            .padding((rowWidth.value * 0.05).dp)
            .clip(RoundedCornerShape((rowWidth.value * 0.1).dp))
            .background(color.value),
            contentAlignment = Alignment.Center
        ){
            AnimatedContent(targetState = count, transitionSpec = {
                scaleIn(animationSpec = tween(animSpeed)) with scaleOut(animationSpec = tween(animSpeed))
            }){ number ->

                LaunchedEffect(Unit){
                    count = b.targetBox.number
                }
                Text(text = number.toString(), fontSize = rowWidth.value.sp / 2, color = Color.White.copy())
            }
        }
    }
}


@Composable
fun DrawMergingBoxes(
    mergingBoxes: State<List<MergingBox>>,
    rowWidth: Dp
){
    mergingBoxes.value.forEach {
        DrawNumBox(
            numBox = it.numBox,
            rowWidth = rowWidth,
            x = it.x,
            y = it.y
        )
    }
}

@Composable
fun DrawFallingBoxes(
    fallingBoxes: State<List<FallingBox>>,
    rowWidth: Dp
){

    fallingBoxes.value.forEach {
        DrawNumBox(
            numBox = it.numBox,
            rowWidth = rowWidth,
            x = it.x,
            y = it.y
        )
    }
}


@Composable
fun DrawBoard(
    board: State<Array<Array<NumBox?>>>,
    rowWidth: Dp
){

//    println("BOARD DRAWING")

    board.value.forEachIndexed { y, numBoxes ->
        numBoxes.forEachIndexed { x, numBox ->
            if(numBox != null){
                DrawNumBox(
                    numBox = numBox,
                    rowWidth = rowWidth,
                    x = x.toFloat(),
                    y = y.toFloat()
                )
            }
        }
    }
}



@Composable
fun DrawCurNum(
    curNum : State<FallingBox>,
    rowWidth : Dp
    ){

    if(curNum.value.numBox != NumBox.NUM_BLANK){

        DrawNumBox(
            numBox = curNum.value.numBox,
            rowWidth = rowWidth,
            x = curNum.value.x,
            y = curNum.value.targetY.toFloat(),
            alpha = 0.2f
        )

        DrawNumBox(
            numBox = curNum.value.numBox,
            rowWidth = rowWidth,
            x = curNum.value.x,
            y = curNum.value.y
        )
    }
}


@Composable
fun DrawNumBox(
    numBox: NumBox,
    x: Float,
    y: Float,
    rowWidth: Dp,
    alpha: Float = 1f
){
    Box(modifier = Modifier.offset(
        x = (x * rowWidth.value).dp,
        y = (y * rowWidth.value).dp
    )
        .size(rowWidth)
        .padding((rowWidth.value * 0.05).dp)
        .clip(RoundedCornerShape((rowWidth.value * 0.1).dp))
        .background(numBox.color.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ){
        Text(text = numBox.number.toString(), fontSize = rowWidth.value.sp / 2, color = Color.White.copy(alpha = alpha))
    }
}