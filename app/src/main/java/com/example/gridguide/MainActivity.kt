package com.example.gridguide

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridguide.ui.theme.GridGuideTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

    lateinit var scrollState: ScrollableState
    var fixedColumnWidth: Int = 0
    var maxProgramCellWidth: Int = 0

    val maxRows = 400
    val maxColumns = 14 * 24 * 2 // 14 days future data in advance
    val minColumns = -3 * 24 * 2 // 3 days previous data in advance
    val rowHeight = 60

    // @Volatile
    lateinit var stateRowX: LazyListState

    //@Volatile
    lateinit var stateRowY: LazyListState

    @OptIn(ExperimentalFoundationApi::class)
    val customPageSize = object : PageSize {
        override fun Density.calculateMainAxisPageSize(
            availableSpace: Int,
            pageSpacing: Int
        ): Int {
            val visiblePageCount = pageCountOnScreen()
            return (availableSpace - (visiblePageCount - 1) * pageSpacing) / visiblePageCount
        }
    }

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
                    /* Below line does not work from 2nd page onwards, so added alternative approach under "LaunchedEffect" temporarily */
                    stateRowY.scrollBy(-delta)
                    // Log.i("Rupayan","After stateRowX scrollBy ${stateRowX.scrollBy(-delta)} stateRowY scrollBy ${stateRowY.scrollBy(-delta)}")
                    /* for(state in stateYList) {
                          state.scrollBy(-delta)
                      }*/
                }
                delta
            }

            /* Below two LaunchedEffects are added to sync stateRowX & stateRowY from 2nd page onwards */
            LaunchedEffect(stateRowX.firstVisibleItemScrollOffset) {
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
            }

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
                text = "GridGuidePOC1",
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
                    .background(color = colorResource(id = R.color.tivo_dark_surface))
            ) {
                ChannelHeaderWithList()
                Spacer(modifier = Modifier.width(2.dp))
                ProgramList()
            }
        }
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ProgramList(modifier: Modifier = Modifier) {
        val pagerState = rememberPagerState(initialPage = -(minColumns))
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            HorizontalPager(
                pageCount = maxColumns + (-1) * minColumns,
                pageSize = customPageSize,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { currentPage ->
                Log.i("Rupayan", "Drawing pager for page : " + currentPage)
                val displayPageNumber = currentPage + minColumns
                Column {
                    TimeSlotCell(rowHeight, "T ${displayPageNumber}")
                    LazyColumn(
                        state = stateRowY,
                        userScrollEnabled = false
                    ) {
                        //Log.i("Rupayan", "LazyColumn of program redrawing")
                        itemsIndexed(channelProgramData) { index, item ->
                            Log.i("Rupayan", "LazyColumn of program drawing row $index")
                            Spacer(modifier = Modifier.height(2.dp))
                            ProgramItemCell(rowHeight, "P - ${index} - ${displayPageNumber}")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ProgramItemCell(height: Int, content: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(colorResource(id = R.color.tivo_dark_surface))
                .paint(
                    painterResource(id = R.drawable.menu__1_),
                    contentScale = ContentScale.FillBounds
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "New",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = content,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White
                    )
                )
            }
        }
    }

    @Composable
    fun TimeSlotCell(height: Int, content: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .background(colorResource(id = R.color.tivo_dark_surface))
                .height(height.dp)
                .paint(
                    painterResource(id = R.drawable.menu__1_),
                    contentScale = ContentScale.FillBounds
                )
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                )
            )
        }
    }

    @Composable
    fun ChannelHeaderWithList(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxWidth(.4f)
        ) {
            ChannelHeaderCell(rowHeight, "Channel Filter")
            ChannelList()
        }
    }

    @Composable
    fun ChannelList() {
        LazyColumn(
            state = stateRowX,
            userScrollEnabled = false
        ) {
            Log.i("Rupayan", "LazyColumn of channel redrawing")
            items(channelProgramData.size) { index ->
                // Log.d("TAG","DrawaingRowNumber==$index")
                Log.i("Rupayan", "LazyColumn of channel drawing row $index")
                Spacer(modifier = Modifier.height(2.dp))
                ChannelItemCell(rowHeight, channelProgramData[index].name)
            }
        }
    }

    @Composable
    fun ChannelHeaderCell(height: Int, content: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .background(colorResource(id = R.color.tivo_dark_surface))
                .height(height.dp)
                .paint(
                    painterResource(id = R.drawable.menu__1_),
                    contentScale = ContentScale.FillBounds
                )
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                )
            )
        }
    }

    @Composable
    fun ChannelItemCell(height: Int, content: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_espn),
                contentDescription = "",
                modifier = Modifier
                    .width(65.dp)
                    .height(60.dp)
                    .align(Alignment.Center)
            )
        }
    }

    private var channelProgramData: ArrayList<ChannelProgramData> = ArrayList()

    private var timeslots: ArrayList<CellItemData> = ArrayList()

    private fun initialize() {
        loadLargeData()
    }

    private fun loadLargeData() {
        for (row in 0..maxRows - 1) {
            val name = "C$row"
            val programList: ArrayList<CellItemData> = ArrayList()
            // for (col in minColumns..maxColumns) {
            // for (col in 0..1) { // making less as this is not used now
            //  if (row % 4 == 0 && col % 2 != 0) {
            //      programList.add(CellItemData(String.format("P-%d-%d", row, col), 15))
            // } else {
            //   programList.add(CellItemData(String.format("P-%d-%d", row, col), 30))
            //  }
            //  }
            channelProgramData.add(ChannelProgramData(name, programList))
        }
        /* for (t in minColumns..maxColumns) {
             timeslots.add(CellItemData("T$t", 30))
         }*/
    }

    private fun isPhoneUi(): Boolean {
        return applicationContext.resources.getBoolean(R.bool.is_phone)
    }

    private fun isPortrait(): Boolean {
        return applicationContext.resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
    }

    private fun pageCountOnScreen(): Int {
        var count = 1;
        if (!isPhoneUi()) {
            if (isPortrait()) count = 2 else count = 3
        }
        return count
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