import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

        val isInvalidInput = mutableStateOf(false)

        val clickHighlight : MutableState<UserInputEffects.ClickHighlight?> = mutableStateOf(null)

        val animFallDuration = remember { BOARD_HEIGHT * 1000 / FALL_SPEED }

        val windowHeightDp = getWindowHeight()

        val boxesQueue = gameVM.queue

        LaunchedEffect(Unit){

            var errorShowTime : Job? = null

            gameVM.userInputEffects.collectLatest {

                when(it){
                    is UserInputEffects.ClickHighlight -> {
                        clickHighlight.value = it
                    }

                    UserInputEffects.InvalidInput -> {
                        isInvalidInput.value = true
                        errorShowTime?.cancel()
                        errorShowTime = launch {
                            delay(300)
                            isInvalidInput.value = false
                        }
                    }
                }
            }
        }




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



        Column (modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Column(modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .weight(1f)
                .zIndex(666f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                    text = "Score : 6903",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.6f)
                    )

                Spacer(modifier = Modifier.heightIn(10.dp))

                Row (modifier = Modifier
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Next box",
                        modifier = Modifier.padding(start = 10.dp),
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )

                    DrawBoxesQueue(boxesQueue)

                }

            }

            BoxWithConstraints(
                modifier = Modifier
                    .heightIn(max = (windowHeightDp - 200).dp)
                    .aspectRatio(BOARD_WIDTH / BOARD_HEIGHT.toFloat())
            ) {

                SideEffect {
                    boardWidth = maxWidth
                }

                val rowWidth = maxWidth / BOARD_WIDTH



                Row (modifier = Modifier
                    .fillMaxSize()
                    .padding(top = rowWidth)
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
                            .background(color = if(isEven) BG_8 else BG_9)
                        )
                    }
                }


                HighlightClicks(
                    click = clickHighlight,
                    rowWidth = rowWidth,
                    maxHeight = maxHeight,
                    animSpeed = animFallDuration
                )


                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowWidth)
                        .background(Color.DarkGray)
                ) {
                    repeat(BOARD_WIDTH){

                        val isEven = it % 2 == 0

                        Box (modifier = Modifier
                            .fillMaxHeight()
                            .width(rowWidth)
                            .padding((rowWidth * 0.05f))
                            .clip(RoundedCornerShape(rowWidth * 0.1f))
                            .background(color = if(isEven) BG_6 else BG_7),
                        ) {

                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.DarkGray,
                                modifier = Modifier.fillMaxSize()
                            )

                        }
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

                ShowInvalidInput(isInvalidInput)

            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.DarkGray))


        }
    }
}


@Composable
fun DrawBoxesQueue(
    queue: StateFlow<List<NumBox>>
){

    val isAnimate = remember { mutableStateOf(false) }

    val offsetX = animateDpAsState(
        targetValue = if(isAnimate.value) 0.dp else 50.dp,
        animationSpec = tween(
            durationMillis = if(isAnimate.value) 300 else 0, easing = LinearEasing)
    )

    val boxesQueue = remember { mutableStateOf<List<NumBox>>(emptyList()) }

    LaunchedEffect(Unit){

        queue.collectLatest {

            isAnimate.value = false

            boxesQueue.value = it

            delay(10)

            isAnimate.value = true

        }
    }


    Row(
        modifier = Modifier.offset(x = if(isAnimate.value) offsetX.value else 50.dp)
    ) {

        queue.value.forEach {

            DrawNumBox(
                numBox = it,
                x = 0f,
                y = 0f,
                rowWidth = 50.dp
            )
        }
    }

}

@Composable
fun HighlightClicks(
    click : MutableState<UserInputEffects.ClickHighlight?>,
    rowWidth: Dp,
    maxHeight : Dp,
    animSpeed: Int
){
    click.value?.let { cl ->

        val isToShow = remember(cl) { MutableTransitionState(false).apply {
            targetState = true
        } }


        AnimatedVisibility(
            visibleState = isToShow,
            enter = slideInVertically(animationSpec = tween(animSpeed, easing = LinearEasing), initialOffsetY = {
                -it * 2
            }),
            exit = fadeOut(animationSpec = tween(0)),
            modifier = Modifier.offset(x = rowWidth * cl.col, y = maxHeight)
        ){

            LaunchedEffect(cl){
                delay(animSpeed.toLong())
                isToShow.targetState = false
            }

            Box(modifier = Modifier
                .fillMaxHeight()
                .width(rowWidth)
                .background(brush = Brush.linearGradient(
                    listOf(
                        Color.Transparent, cl.color, cl.color, Color.Transparent
                    )
                ), shape = RectangleShape, alpha = 0.2f)
            )
        }
    }
}



@Composable
fun ShowInvalidInput(
    isInvalidInput: MutableState<Boolean>
){

    AnimatedVisibility(
        visible = isInvalidInput.value,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(400))
    ){
        Box(modifier = Modifier
            .fillMaxSize()
            .border(width = 5.dp, brush = Brush.verticalGradient(
                listOf(
                    Color.Red, bronze, Color.Transparent
                )
            ), shape = RectangleShape)
        )
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

        var count by remember { mutableStateOf(b.startBox.label)}

        Box(modifier = Modifier.offset(
            x = (rowWidth * b.x),
            y = (rowWidth * b.y)
        )
            .size(rowWidth)
            .padding((rowWidth * 0.05f))
            .clip(RoundedCornerShape((rowWidth * 0.1f)))
//            .border(width = (rowWidth * 0.03f), color = b.startBox.border)
            .background(color.value),
            contentAlignment = Alignment.Center
        ){
            AnimatedContent(targetState = count, transitionSpec = {
                scaleIn(animationSpec = tween(animSpeed)) with scaleOut(animationSpec = tween(animSpeed))
            }){ number ->

                LaunchedEffect(Unit){
                    count = b.targetBox.label
                }
                DrawNumberText(number, rowWidth)
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

        if(curNum.value.scale == 1f){
            DrawNumBox(
                numBox = curNum.value.numBox,
                rowWidth = rowWidth,
                x = curNum.value.x,
                y = curNum.value.targetY.toFloat(),
                alpha = 0.2f
            )
        }

        DrawNumBox(
            numBox = curNum.value.numBox,
            rowWidth = rowWidth,
            x = curNum.value.x,
            y = curNum.value.y,
            scale = curNum.value.scale
        )
    }
}


@Composable
fun DrawNumBox(
    numBox: NumBox,
    x: Float,
    y: Float,
    rowWidth: Dp,
    alpha: Float = 1f,
    scale : Float = 1f
){
    Box(modifier = Modifier.offset(
        x = (x * rowWidth.value).dp,
        y = (y * rowWidth.value).dp
    )
        .size(rowWidth)
        .padding((rowWidth * 0.05f))
        .clip(RoundedCornerShape((rowWidth * 0.1f)))
        .scale(scale)
//        .border(width = (rowWidth * 0.03f), color = numBox.border.copy(alpha = alpha))
        .background(numBox.color.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ){
        DrawNumberText(numBox.label, rowWidth, alpha)
    }
}

@Composable
fun DrawNumberText(num : String, rowWidth: Dp, alpha: Float = 1f){
    Text(
        text = num,
        fontSize = rowWidth.value.sp / (num.length).coerceAtLeast(2),
        fontWeight = FontWeight.ExtraBold,
        color = Color.White.copy(alpha = alpha)
    )
}
