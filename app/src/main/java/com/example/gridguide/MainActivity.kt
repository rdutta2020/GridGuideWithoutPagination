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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import androidx.lifecycle.ViewModelProvider
import com.example.gridguide.model.GuideCell
import com.example.gridguide.ui.theme.GridGuideTheme
import com.example.gridguide.viewmodel.GuideViewModel
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

    companion object {
        const val TAG = "GuideViewModel"
    }

    lateinit var scrollState: ScrollableState
    var fixedColumnWidth: Int = 0
    var maxProgramCellWidth: Int = 0

    var maxRows = 400
    val maxColumns = 14 * 24 * 2 // 14 days future data in advance
    val minColumns = -3 * 24 * 2 // 3 days previous data in advance
    val rowHeight = 60
    val ONE_SLOT_IN_SEC = 60*30

    // @Volatile
    lateinit var stateRowX: LazyListState

    //@Volatile
    lateinit var stateRowY: LazyListState

    lateinit var guideViewModel : GuideViewModel

    // TODO: May not be required
    var previousLastVisibleIndex = 0
    var mFirstVisibleIndex = 0
    var mLastVisibleIndex = 0

    @OptIn(ExperimentalFoundationApi::class)
    val customPageSize = object : PageSize {
        override fun Density.calculateMainAxisPageSize(
            availableSpace: Int,
            pageSpacing: Int
        ): Int {
            val visiblePageCount = pageCountOnScreen()
            return (availableSpace - (visiblePageCount - 1) * pageSpacing) / visiblePageCount

             //return (297 * resources.displayMetrics.density).toInt()
        }
    }

    @SuppressLint(
        "UnusedMaterial3ScaffoldPaddingParameter",
        "UnusedMaterialScaffoldPaddingParameter"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        guideViewModel = ViewModelProvider(this).get(GuideViewModel::class.java)
        maxRows = guideViewModel.totalChannelCount
        initialize()
        setContent {
            stateRowX = rememberLazyListState() // State for the first Row, X
            stateRowY = rememberLazyListState() // State for the second Row, Y

            val scope = rememberCoroutineScope()
            val firstVisibleIndex = remember {
                derivedStateOf {
                    stateRowY.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                }
            }

            val lastVisibleIndex = remember {
                derivedStateOf {
                    stateRowY.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                }
            }

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

            val lastVisibleItemIndex = remember {
                derivedStateOf {
                    stateRowY.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                }
            }

            val firstVisibleItemIndex = remember {
                derivedStateOf {
                    stateRowY.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                }
            }


            LaunchedEffect(key1 = lastVisibleItemIndex.value) {
                Log.i("Rupayan1", "firstItemIndex ${firstVisibleItemIndex.value} lastItemIndex ${lastVisibleItemIndex.value} previousLastVisibleIndex ${previousLastVisibleIndex}")
                mFirstVisibleIndex = firstVisibleItemIndex.value!!
                mLastVisibleIndex = lastVisibleItemIndex.value!!

                var scrollingUp = false
                lastVisibleItemIndex.value?.let {
                    if(previousLastVisibleIndex < it){
                        Log.i("Rupayan1", "Scrolling down")
                    }else{
                        Log.i("Rupayan1", "Scrolling up")
                        scrollingUp = true
                    }
                    previousLastVisibleIndex = lastVisibleItemIndex.value!!
                }
                val currentTimeSlotStartTime =
                    findStartTimeOfTimeSlot(System.currentTimeMillis() / 1000)
                firstVisibleItemIndex.value?.let {firstIndex ->
                    lastVisibleItemIndex.value?.let { lastIndex ->
                        guideViewModel.fetchGuideDataIfRequired(
                            currentTimeSlotStartTime,
                            firstIndex,
                            lastIndex)
                        /*if(scrollingUp) {
                            guideViewModel.fetchGuideDataIfRequired(
                                currentTimeSlotStartTime,
                                firstIndex + 1,
                                lastIndex
                            )
                        }else{
                            guideViewModel.fetchGuideDataIfRequired(
                                currentTimeSlotStartTime,
                                firstIndex,
                                lastIndex+1
                            )
                        }*/
                    }
                }
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

/*
            Log.i("Rupayan1", "first visible :  ${stateRowY.layoutInfo.visibleItemsInfo.firstOrNull()?.index} " +
                    "last visible:  ${stateRowY.layoutInfo.visibleItemsInfo.lastOrNull()?.index} " +
                    "total: ${stateRowY.layoutInfo.totalItemsCount}")
*/
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
    private fun LazyListState.isScrollingUp(): Boolean {
        var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
        var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
        return remember(this) {
            derivedStateOf {
                if (previousIndex != firstVisibleItemIndex) {
                    previousIndex > firstVisibleItemIndex
                } else {
                    previousScrollOffset >= firstVisibleItemScrollOffset
                }.also {
                    previousIndex = firstVisibleItemIndex
                    previousScrollOffset = firstVisibleItemScrollOffset
                }
            }
        }.value
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
                state = pagerState
               // modifier = Modifier.weight(1f)
            ) { currentPage ->

                val displayPageNumber = currentPage + minColumns
                //Log.i(TAG, "Drawing pager for page : " + displayPageNumber)
                val currentTimeSlotStartTime =
                    findStartTimeOfTimeSlot(System.currentTimeMillis() / 1000)
                val currentPageSlotStartTime =
                    currentTimeSlotStartTime + (displayPageNumber * ONE_SLOT_IN_SEC)
                Column {
                    TimeSlotCell(rowHeight, "T ${currentPageSlotStartTime}+({$displayPageNumber})")
                    LazyColumn(
                        state = stateRowY,
                        userScrollEnabled = false
                    ) {
                        //Log.i("Rupayan", "LazyColumn of program redrawing")
                        //Log.i("SHYAKDAS", "Page ${currentPageSlotStartTime}")
                       // Log.i(TAG, "Drawing pager for timeslot: " + currentPageSlotStartTime)
                        // check if additional data fetching is required for this page and do if required
                        guideViewModel.fetchGuideDataIfRequired(currentPageSlotStartTime, mFirstVisibleIndex, mLastVisibleIndex)
                        val programListForTimeSlot: SnapshotStateList<ProgramsForTimeSlot> =
                            guideViewModel.startTimeToProgramListMap.get(currentPageSlotStartTime)!!
                        /*Log.i(
                            "SHYAKDAS",
                            "Page ${currentPageSlotStartTime} Data size ${programListForTimeSlot.size}"
                        )*/
                        itemsIndexed(programListForTimeSlot) { index, item ->
                           // Log.i("Rupayan3", "LazyColumn of program drawing row $index")
                            Spacer(modifier = Modifier.height(2.dp))
                            LazyRow(userScrollEnabled = false) {
                                itemsIndexed(programListForTimeSlot.get(index).programs) { indexRow, itemRow ->
                                    if(indexRow == 1){
                                        Log.i("Rupayan2", "Two programs in  time slot $displayPageNumber and row $indexRow")
                                    }
                                    val widthFraction =
                                        if (itemRow.duration >= ONE_SLOT_IN_SEC) 1f else (itemRow.duration.toFloat() / ONE_SLOT_IN_SEC.toFloat())
                                    Log.i("Rupayan2", "Two programs in  time slot $displayPageNumber row $index column $indexRow widthFraction $widthFraction duration ${itemRow.duration} title ${itemRow.title}")
                                    ProgramItemCell(rowHeight, itemRow.contentId, widthFraction)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun findStartTimeOfTimeSlot(second: Long) : Long {
        val fraction = second / ONE_SLOT_IN_SEC
        return fraction * ONE_SLOT_IN_SEC
    }

    @Composable
    fun ProgramItemCell(height: Int, content: String, widthFraction: Float) {
        Box(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(height.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(colorResource(id = R.color.tivo_dark_surface))
               /* .paint(
                    painterResource(id = R.drawable.menu__1_),
                    contentScale = ContentScale.FillBounds
                )*/
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                  //  .padding(5.dp)
            ) {
                   /* Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        if (rowNumber % 4 == 0) {
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
                        }
                        Text(
                            text = "${rowNumber * 400 / 30}m left",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        )
                    }*/
                  //  Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = content,
                        maxLines = 1,
                       // overflow = TextOverflow.Ellipsis,
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
            itemsIndexed(guideViewModel.stationIds) { index, item ->
                // Log.d("TAG","DrawaingRowNumber==$index")
               // Log.i("Rupayan", "LazyColumn of channel drawing row $index")
                Spacer(modifier = Modifier.height(2.dp))
                ChannelItemCell(rowHeight, item)
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
    fun ChannelItemCell(height: Int, channelData: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
        ) {
            /*Image(
                painter = painterResource(id = getIconDrawable(rowNumber)),
                contentDescription = "",
                modifier = Modifier
                    .width(65.dp)
                    .height(60.dp)
                    .align(Alignment.Center)
            )*/
            Text(
                text = channelData,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White
                )
            )
        }
    }

    private fun getIconDrawable(rowNumber: Int): Int=
        when(rowNumber%10){
            1->R.drawable.ic_1
            2->R.drawable.ic_2
            3->R.drawable.ic_3
            4->R.drawable.ic_4
            5->R.drawable.ic_5
            6->R.drawable.ic_6
            7->R.drawable.ic_7
            8->R.drawable.ic_8
            9->R.drawable.ic_9
            else->R.drawable.ic_10
        }

    private fun getProgramName(rowNumber: Int, timeslotNumber: Int): String=
        when((rowNumber+1)*(Math.abs(timeslotNumber)+1) %10){
            1->"Golden State Warrior"
            2->"La Reina Del Sur"
            3->"Mi Camino Es Amarte"
            4->"Roads War"
            5->"DateLine"
            6->"Nature Cat"
            7->"Donkey Hodie"
            8->"In the Heart of the Night"
            9->"Moon 101"
            else->"NBA Today"
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

data class ProgramsForTimeSlot(
    val programs: ArrayList<GuideCell>
)

enum class ListState {
    IDLE,
    LOADING,
    PAGINATING,
    ERROR,
    PAGINATION_EXHAUST,
}