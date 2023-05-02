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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridguide.ui.theme.GridGuideTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

    lateinit var globalState: ScrollState
    var fixedColumnWidth: Int = 0
    var maxProgramCellWidth: Int = 0

    val maxRows = 15
    val maxColumns = 12 //672
    val rowHeight = 60

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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            TopBar(lazyListState = lazyListState)
                            MainContent(lazyListState = lazyListState)
                        }
                    }
                )
            }
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
    fun MainContent(lazyListState: LazyListState) {
        val padding by animateDpAsState(
            targetValue = if (lazyListState.isScrolled) 0.dp else TOP_BAR_HEIGHT,
            animationSpec = tween(durationMillis = 300)
        )
        Column(
            modifier = Modifier
                .padding(top = padding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                ChannelHeaderWithList()
                ProgramList()
            }
        }
    }


    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun ProgramList(modifier: Modifier = Modifier) {
        val pagerState = rememberPagerState()

        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            HorizontalPager(
                count = 672,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { currentPage ->
                Column {
                    ItemCell(rowHeight, "T ${currentPage + 1}")
                    LazyColumn{
                        itemsIndexed(channelProgramData) { index, item ->
                            ItemCell(rowHeight, "P - ${index + 1} - ${currentPage + 1}")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ChannelHeaderWithList(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxWidth(.4f)
        ) {
            ItemCell(rowHeight, "Channel Filter")
            ChannelList()
        }
    }

    @Composable
    fun ChannelList() {
        LazyColumn() {
            items(channelProgramData.size) { index ->
                Log.d("TAG","DrawaingRowNumber==$index")
                ItemCell(rowHeight, channelProgramData[index].name)
            }
        }
    }

    @Composable
    fun ItemCell(height: Int, content: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, SolidColor(Color.Blue)))
                .height(height.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black
                )
            )
        }
    }


    private var channelProgramData: ArrayList<ChannelProgramData> = ArrayList()

    private var timeslots: ArrayList<CellItemData> = ArrayList()

    private fun initialize() {
        loadLargeData()
    }

    private fun loadLargeData() {
        for (row in 1..maxRows) {
            val name = "C$row"
            val programList: ArrayList<CellItemData> = ArrayList()
            for (col in 1..maxColumns) {
                if (row % 4 == 0 && col % 2 != 0) {
                    programList.add(CellItemData(String.format("P-%d-%d", row, col), 15))
                } else {
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