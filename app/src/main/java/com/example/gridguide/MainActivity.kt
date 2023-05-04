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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

    lateinit var globalState: ScrollState

    lateinit var scrollState: ScrollableState
    var fixedColumnWidth: Int = 0
    var maxProgramCellWidth: Int = 0

    val maxRows = 400
    val maxColumns = 672
    val rowHeight = 60

   // @Volatile
    lateinit var stateRowX : LazyListState
    //@Volatile
    lateinit var stateRowY : LazyListState

    val stateYList = ArrayList<LazyListState>()

        @SuppressLint(
        "UnusedMaterial3ScaffoldPaddingParameter",
        "UnusedMaterialScaffoldPaddingParameter"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setContent {
            stateRowX = rememberLazyListState() // State for the first Row, X
            stateRowY = rememberLazyListState() // State for the second Row, Y

            val scope = rememberCoroutineScope()
            scrollState = rememberScrollableState { delta ->
                Log.i("Rupayan", "scrollState changed by $delta")
                scope.launch {
                    //Log.i("Rupayan","Before stateRowX ${stateRowX.firstVisibleItemIndex} stateRowY ${stateRowY.firstVisibleItemIndex}")
                    stateRowX.scrollBy(-delta)

                    stateRowY.scrollBy(-delta)
                   // Log.i("Rupayan","After stateRowX scrollBy ${stateRowX.scrollBy(-delta)} stateRowY scrollBy ${stateRowY.scrollBy(-delta)}")
                  /* for(state in stateYList) {
                        state.scrollBy(-delta)
                    }*/
                }
                delta
            }

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

          /*  LaunchedEffect(stateRowX.firstVisibleItemScrollOffset) {
                if (!stateRowY.isScrollInProgress) {
                    stateRowY.scrollToItem(
                        stateRowX.firstVisibleItemIndex,
                        stateRowX.firstVisibleItemScrollOffset
                    )
                }
            }

            LaunchedEffect(stateRowY.firstVisibleItemScrollOffset) {
                if (!stateRowX.isScrollInProgress) {
                    stateRowX.scrollToItem(
                        stateRowY.firstVisibleItemIndex,
                        stateRowY.firstVisibleItemScrollOffset
                    )
                }
            }*/
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
                    .scrollable(scrollState, Orientation.Vertical)
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
       /* var stateY = rememberLazyListState()
        if(stateYList.size > 1){
            stateY = stateYList[stateYList.size-1]
        }
        stateYList.add(stateY)*/
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            HorizontalPager(
                count = maxColumns,// INT MAX should be
               // state = pagerState,
                modifier = Modifier.weight(1f)
            ) { currentPage ->
                Log.i("Rupayan", "Drawing pager for page : "+currentPage)
                Column {
                    ItemCell(rowHeight, "T ${currentPage + 1}")
                    LazyColumn(state = stateRowY,
                        userScrollEnabled = false){
                        //Log.i("Rupayan", "LazyColumn of program redrawing")
                        itemsIndexed(channelProgramData) { index, item ->
                            Log.i("Rupayan", "LazyColumn of program drawing row $index")
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
        LazyColumn(state = stateRowX,
            userScrollEnabled = false) {
            Log.i("Rupayan", "LazyColumn of channel redrawing")
            items(channelProgramData.size) { index ->
               // Log.d("TAG","DrawaingRowNumber==$index")
                Log.i("Rupayan", "LazyColumn of channel drawing row $index")
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