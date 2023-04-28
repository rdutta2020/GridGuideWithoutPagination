package com.example.gridguide

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridguide.ui.theme.GridGuideTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

    lateinit var globalState: ScrollState
    var fixedColumnWidth: Int = 0
    var maxProgramCellWidth: Int = 0


    val maxRows = 400
    val maxColumns = 12 //672

    @SuppressLint(
        "UnusedMaterial3ScaffoldPaddingParameter",
        "UnusedMaterialScaffoldPaddingParameter"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setContent {
            globalState = rememberScrollState()
            fixedColumnWidth = 128
            maxProgramCellWidth = LocalConfiguration.current.screenWidthDp - fixedColumnWidth
            GridGuideTheme(darkTheme = false) {
                val lazyListState = rememberLazyListState()
                Scaffold(
                    content = {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            MainContent(lazyListState = lazyListState)
                            TopBar(lazyListState = lazyListState)
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun MainContent(lazyListState: LazyListState) {
        val padding by animateDpAsState(
            targetValue = if (lazyListState.isScrolled) 0.dp else TOP_BAR_HEIGHT,
            animationSpec = tween(durationMillis = 300)
        )

        Column(
            modifier = Modifier.padding(top = padding)
        ) {
            DrawHeader()
            DrawGrid(lazyListState)
        }
    }

    @Composable
    fun TopBar(lazyListState: LazyListState) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.primary)
                .animateContentSize(animationSpec = tween(durationMillis = 300))
                .height(height = if (lazyListState.isScrolled) 0.dp else TOP_BAR_HEIGHT),
            contentPadding = PaddingValues(start = 16.dp)
        ) {
            Text(
                text = "GridGuidePOC",
                style = TextStyle(
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    color = MaterialTheme.colors.surface
                )
            )
        }
    }

    @Composable
    fun DrawGrid(scrollState: LazyListState) {
        LazyColumn(
             // state = scrollState
        ) {
            items(channelProgramData) { rowdata ->
                DrawGridRowItem(item = rowdata)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun DrawHorizontalList(rowItems: ArrayList<CellItemData>, height: Int) {
        val lazyListState = rememberLazyListState()
        val snappingLayout =
            remember(lazyListState) { CreateSnapLayoutInfoProvider(maxColumns, globalState) }
        val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

        LazyRow(
            modifier = Modifier
                .horizontalScroll(globalState)
                .width((maxColumns * maxProgramCellWidth).dp),
            state = lazyListState,
            flingBehavior = flingBehavior
        ) {
            items(rowItems) { cellItem ->
                DrawRowListItem(cellData = cellItem, height = height)
            }
        }
    }

    @ExperimentalFoundationApi
    fun CreateSnapLayoutInfoProvider(
        itemCount: Int,
        scrollState: ScrollState,
    ): SnapLayoutInfoProvider = object : SnapLayoutInfoProvider {
        override fun Density.calculateApproachOffset(initialVelocity: Float): Float = 0f
        override fun Density.calculateSnapStepSize(): Float {
            return scrollState.maxValue.toFloat() / (itemCount - 1)
        }

        override fun Density.calculateSnappingOffsetBounds(): ClosedFloatingPointRange<Float> {
            val bound0 = -scrollState.value % calculateSnapStepSize()
            val bound1 = calculateSnapStepSize() + bound0

            return (if (bound0 >= 0 && bound1 < 0) bound1.rangeTo(bound0) else bound0.rangeTo(bound1))
        }
    }

    @Composable
    fun DrawRowListItem(
        cellData: CellItemData,
        height: Int
    ) {
        var fraction = 1F
        if(cellData.duration != 30){
            fraction = (cellData.duration).toFloat() / 30F
        }
        Box(
            modifier = Modifier
                .width((maxProgramCellWidth * fraction).dp)
                .border(BorderStroke(1.dp, SolidColor(Color.Blue)))
                .height(height.dp)
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = cellData.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black
                )
            )
        }
    }

    @Composable
    fun DrawGridRowItem(
        item: ChannelProgramData
    ) {
        Box(
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(fixedColumnWidth.dp)
                        .border(BorderStroke(1.dp, SolidColor(Color.Blue)))
                        .height(60.dp), contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            //.padding(top = 2.dp)
                            .align(Alignment.Center),
                        text = item.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    )
                }
                DrawHorizontalList(item.programList, 60)
            }
        }
    }


    @Composable
    fun DrawHeader() {
        Box(
            modifier = Modifier
                .height(30.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(fixedColumnWidth.dp)
                        .border(BorderStroke(1.dp, SolidColor(Color.Blue)))
                        .height(30.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center),
                        text = "Channel Filter",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            // fontFamily = FontFamily(Font(R.font.roboto_regular, FontWeight.Normal)),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    )
                }
                DrawHorizontalList(timeslots, 30)
            }
        }
    }


    private var channelProgramData: ArrayList<ChannelProgramData> = ArrayList()

    private var timeslots: ArrayList<CellItemData> = ArrayList()
    private var durationSlots: ArrayList<Int> = ArrayList()

    private fun initialize() {
        loadLargeData()
        //loadSmallData()
    }

    private fun loadLargeData() {
        for (row in 1..maxRows) {
            val name = "C$row"
            val programList: ArrayList<CellItemData> = ArrayList()
            for (col in 1..maxColumns) {
                if(row %4 == 0 && col % 2 != 0){
                    programList.add(CellItemData(String.format("P-%d-%d", row, col), 15))
                }else{
                    programList.add(CellItemData(String.format("P-%d-%d", row, col), 30))
                }
            }
            channelProgramData.add(ChannelProgramData(name, programList))
        }
        for (t in 1..maxColumns) {
            timeslots.add(CellItemData("T$t", 30))
        }
    }
}


val TOP_BAR_HEIGHT = 56.dp
val LazyListState.isScrolled: Boolean
    get() = firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0

data class ChannelProgramData(
    val name: String,
    val programList: ArrayList<CellItemData>
)

data class CellItemData(
    val name: String,
    val duration: Int
)