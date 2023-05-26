package com.example.gridguide.viewmodel

import android.os.Process
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gridguide.ListState
import com.example.gridguide.MainActivity
import com.example.gridguide.ProgramsForTimeSlot
import com.example.gridguide.model.ContentType
import com.example.gridguide.model.GuideCell
import com.example.gridguide.model.GuideRow
import com.example.gridguide.network.RetrofitInstance
import com.squareup.moshi.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class GuideViewModel : ViewModel() {
    companion object {
        const val TAG = "GuideViewModel"
        private var incrementCounter=0
    }

    val stationIds = listOf(
        "tivo:st.10420136",
        "tivo:st.10420224",
        "tivo:st.10420137",
        "tivo:st.10420139",
        "tivo:st.10420240",
        "tivo:st.382994517",
        "tivo:st.72587930",
        "tivo:st.261156207",
        "tivo:st.110901431",
        "tivo:st.988",
        "tivo:st.157462615",
        "tivo:st.366229821",
        "tivo:st.384530143",
        "tivo:st.12217684",
        "tivo:st.406397",
        "tivo:st.354323064",
        "tivo:st.169356734",
        "tivo:st.361372665",
        "tivo:st.361372666",
        "tivo:st.343479906",
        "tivo:st.361372667",
        "tivo:st.364871206",
        "tivo:st.345316909",
        "tivo:st.384488735",
        "tivo:st.454567727",
        "tivo:st.1141660",
        "tivo:st.442803409",
        "tivo:st.427517047",
        "tivo:st.427517045",
        "tivo:st.335",
        "tivo:st.709",
        "tivo:st.1927",
        "tivo:st.806",
        "tivo:st.125469377",
        "tivo:st.813",
        "tivo:st.987",
        "tivo:st.2324",
        "tivo:st.111685056",
        "tivo:st.1352533",
        "tivo:st.17788147"
    )

    val NUMBER_OF_STATIONS_PER_CALL = 10
    val TIME_DURATION_PER_CALL = 3 * 60 * 60 // we ask for 3hr data in one guideRow call
    val DURATION_PER_SLOT = 30 * 60 // 30 mins in seconds

    val ONE_SLOT_IN_SEC = 60 * 30

    val startTimeToProgramListMap = mutableMapOf<Long, SnapshotStateList<ProgramsForTimeSlot>>()
    val startTimeToProgramListAvailabilityMap = mutableMapOf<Long, BooleanArray>()
    var lastCallStartTime = 0L
    lateinit var lastCallStationIds: List<String>
    var listState by mutableStateOf(ListState.IDLE)
   // var lastGuideCallStartStationIdIndex = 0
   // var lastGuideCallEndStationIdIndex = 0
    val totalChannelCount = stationIds.size
    lateinit var dummyProgramsForTimeSlot : ArrayList<ProgramsForTimeSlot>

    // below parameters will be initialized in configure()
    var pagesToLoadPerCall = 0 ; // how many pages are loaded per call

    init {
        configure()
        createDummyProgramList()
        val currentTime = MainActivity.findStartTimeOfTimeSlot(System.currentTimeMillis() / 1000)
        createTimeSlotBasedProgramLists(currentTime)
        fetchGuideDataIfRequired(currentTime, 0, 0)
    }

    private fun configure() {
        pagesToLoadPerCall = TIME_DURATION_PER_CALL / MainActivity.timeDurationPerPage
        Log.i(TAG,"pagesToLoadPerCall $pagesToLoadPerCall")
    }

    fun fetchGuideDataIfRequired(timeSlot: Long, firstVisibleItemIndex: Int, lastVisibleItemIndex: Int){
        if(listState == ListState.LOADING){
            // TODO : Queuing required otherwise some important request will be ignored
            Log.i(TAG,"already api is running")
            return
        }
        Log.i(TAG,"fetchGuideDataIfRequired timeSlot ${timeSlot} firstVisibleItemIndex " +
                "${firstVisibleItemIndex} lastVisibleItemIndex ${lastVisibleItemIndex}")
        var guideCallStartStationIdIndex = 0
        var guideCallEndStationIdIndex = 0
       // if(startTimeToProgramListAvailabilityMap.containsKey(timeSlot)){
            val programListAvailabilityList = startTimeToProgramListAvailabilityMap.get(timeSlot)
            programListAvailabilityList?.let {
                if(firstVisibleItemIndex < 0 || lastVisibleItemIndex >= totalChannelCount){
                    // we cannot scroll beyond the station list size
                    return
                }
                if(programListAvailabilityList.get(firstVisibleItemIndex) &&
                    programListAvailabilityList.get(lastVisibleItemIndex)){
                    // both indexes' data already fetched, no need to call guide API
                    return
                }else if(!(programListAvailabilityList.get(firstVisibleItemIndex)) &&
                    programListAvailabilityList.get(lastVisibleItemIndex)){
                    // when vertically scrolling up and if data was not fetched earlier
                    guideCallStartStationIdIndex = if (firstVisibleItemIndex > NUMBER_OF_STATIONS_PER_CALL) (firstVisibleItemIndex - NUMBER_OF_STATIONS_PER_CALL) else 0
                }else if(programListAvailabilityList.get(firstVisibleItemIndex) &&
                    !(programListAvailabilityList.get(lastVisibleItemIndex))) {
                    // when vertically scrolling down and if data was not fetched earlier
                    guideCallStartStationIdIndex = lastVisibleItemIndex
                }else{
                    guideCallStartStationIdIndex = firstVisibleItemIndex
                }
            }
     /*   }else{
            // when app is launched for 1st time
            // TODO :: May not be required
            lastGuideCallStartStationIdIndex = firstVisibleItemIndex
        }*/
        listState = ListState.LOADING
        //lastCallStartTime = timeSlot
        //val currentTime = findStartTimeOfTimeSlot(System.currentTimeMillis() / 1000)

        /* we can fetch 4hr data at max from API, but we are fetching 3hr i.e. 6 timeslots as 6 is
        divisible by 3 which is landscape timeslot count per page */

        // TODO : Must fix
        val startTime = timeSlot - 3*DURATION_PER_SLOT
        val endTime = timeSlot + 3*DURATION_PER_SLOT
        lastCallStartTime = startTime
        guideCallEndStationIdIndex = Math.min(guideCallStartStationIdIndex+NUMBER_OF_STATIONS_PER_CALL,totalChannelCount)-1
        // 1st call with current channel list visible
        Log.i(TAG,"guideRows call station list start index ${guideCallStartStationIdIndex} end index ${guideCallEndStationIdIndex} timeSlot $timeSlot")
        getGridGuide(
            prepareStationIdList(guideCallStartStationIdIndex, guideCallEndStationIdIndex),
            startTime, endTime
        )

        // 2nd call with previous channel list (NUMBER_OF_STATIONS_PER_CALL items)
        val prevGuideCallStartStationIdIndex = Math.max(guideCallStartStationIdIndex- NUMBER_OF_STATIONS_PER_CALL, 0)
        val prevGuideCallEndStationIdIndex = Math.min(prevGuideCallStartStationIdIndex + NUMBER_OF_STATIONS_PER_CALL-1, totalChannelCount-1);
        if(prevGuideCallStartStationIdIndex < prevGuideCallEndStationIdIndex) {
            Log.i(TAG,"guideRows call station list prev call start index ${prevGuideCallStartStationIdIndex} end index ${prevGuideCallEndStationIdIndex}  timeSlot $timeSlot")
            getGridGuide(
                prepareStationIdList(prevGuideCallStartStationIdIndex, prevGuideCallEndStationIdIndex),
                startTime, endTime
            )
        }

        // 3rd call with next channel list (NUMBER_OF_STATIONS_PER_CALL items)
        val nextGuideCallStartStationIdIndex = Math.min(guideCallStartStationIdIndex+ NUMBER_OF_STATIONS_PER_CALL, stationIds.size-1)
        val nextGuideCallEndStationIdIndex = Math.min(nextGuideCallStartStationIdIndex + NUMBER_OF_STATIONS_PER_CALL-1, totalChannelCount-1);
        if(nextGuideCallStartStationIdIndex < nextGuideCallEndStationIdIndex) {
            Log.i(TAG,"guideRows call station list next call start index ${nextGuideCallStartStationIdIndex} end index ${nextGuideCallEndStationIdIndex}  timeSlot $timeSlot")
            getGridGuide(
                prepareStationIdList(nextGuideCallStartStationIdIndex, nextGuideCallEndStationIdIndex),
                startTime, endTime
            )
        }


        /*if(timeSlot >= currentTime) {
            getGridGuide(
                prepareStationIdList(),
                timeSlot, timeSlot + DURATION_PER_CALL
            )
        }else{
            getGridGuide(
                prepareStationIdList(),
                timeSlot - DURATION_PER_CALL, timeSlot
            )
        }*/
    }

    private fun prepareStationIdList(startStationIdIndex:Int, endStationIdIndex:Int): String {
       // lastGuideCallEndStationIdIndex = Math.min(lastGuideCallStartStationIdIndex+NUMBER_OF_STATIONS_PER_CALL,totalChannelCount)-1
        val stationIdList = stationIds.subList(startStationIdIndex, endStationIdIndex+1)
        val mutableStationIdList = stationIdList.toMutableList()
        lastCallStationIds = stationIdList
        mutableStationIdList.sort()
        val idListString = StringBuilder()
        for (i in 1..mutableStationIdList.size) {
            if (i != 1) {
                idListString.append(',')
            }
            idListString.append(mutableStationIdList.get(i - 1))
        }
        return idListString.toString()
    }



    fun getGridGuide(stationIdList: String, startTime: Long, endTime: Long) {
        //createTimeSlotBasedProgramLists(startTime)
      //  viewModelScope.launch(Dispatchers.IO) {
            Log.i(TAG,"stationIdList ${stationIdList} startTime ${startTime} endTime ${endTime}")
            val call = RetrofitInstance.api.guideRowsGet(
             //   featureArea = "GridGuide",
                msoPartnerId = "tivo:pt.5058",
                stationId = stationIdList,
                windowStartTime = startTime,
                windowEndTime = endTime,
            /*    applicationVersion = "4.11.0-20230314-0430",
                deviceType = "androidPhone",*/
                requestId = generateRequestId(),
             /*   language = "en-US",
                productName = "Tivo Mobile IPTV v.4.11.0-20230314-0430 Quickdroid",
                amznRequestId = "1678820014055-3047906886224678661",*/

                applicationName = "com.tivo.cableco.debug",
                bodyId = "tsn:A8F0F000021749C",
             /*   encoding = "gzip",
                userAgent = "vscode-restclient"*/
            )
            val t1 = System.currentTimeMillis()
            var t2 = 0L
            call.enqueue(object : Callback<List<GuideRow>> {
                override fun onResponse(
                    call: Call<List<GuideRow>>,
                    response: Response<List<GuideRow>>
                ) {
                  //  viewModelScope.launch(Dispatchers.IO) {
                        t2 = System.currentTimeMillis()
                        Log.d(TAG, "Success Case Time taken in API call (ms) =${t2 - t1}")
                        Log.d(TAG, "APIRESPONSE==${response.body()}")
                        val reponseData = response.body()?.toMutableList()
                        if (reponseData != null) {
                            // ToDO : Station id maps
                            val reponseDataSorted =
                                reponseData.sortedBy { lastCallStationIds.indexOf(it.stationId) }
                            // split the data for "pagesToLoadPerCall" pages
                            for (t in 0..pagesToLoadPerCall-1) {
                                addProgramListForTimeStamp(
                                    reponseDataSorted,
                                    lastCallStartTime + (t * MainActivity.timeDurationPerPage)
                                )
                                //fillProgramAvailabilityListStatus(lastCallStartTime + (t * ONE_SLOT_IN_SEC))
                            }
                        }
                        val t3 = System.currentTimeMillis()
                        Log.d(TAG, "Time taken in data manupulation (ms) =${t3 - t2}")
                        listState = ListState.IDLE
                 //   }
                }

                override fun onFailure(call: Call<List<GuideRow>>, t: Throwable) {
                    // Handle error response
                    t2 = System.currentTimeMillis()
                    Log.d(TAG, "Failure case Time taken in API call (ms) =${t2-t1}")
                    Log.d(TAG, "APIRESPONSE==${t.message}")
                    listState = ListState.IDLE
                }
            })
     //   }
    }

    /*private fun fillProgramAvailabilityListStatus(timeSlot: Long) {
        /*if(!startTimeToProgramListAvailabilityMap.containsKey(timeSlot)){
            startTimeToProgramListAvailabilityMap.put(timeSlot, BooleanArray(totalChannelCount))
        }*/
        val programListAvailabilityStatus = startTimeToProgramListAvailabilityMap.get(timeSlot)
        programListAvailabilityStatus?.let {
            // we have fetched data for below station ids
            for(index in lastGuideCallStartStationIdIndex..lastGuideCallEndStationIdIndex) {
                programListAvailabilityStatus[index] = true
            }
        }
    }*/


    private fun createTimeSlotBasedProgramLists(startTime: Long) {
        // split the data for 8 timeslots (4hr)
        // we need to show 3 days back and 14 days advance data
        val startIndex = -MainActivity.pastPageCount
        val endIndex = MainActivity.futurePageCount-1
        for (t in startIndex..endIndex) {
            val timeSlot = startTime+ (t * MainActivity.timeDurationPerPage)
         //   if (!startTimeToProgramListMap.contains(timeSlot)) {
                // this timeslot is not in map yet, create one
            startTimeToProgramListMap.put(timeSlot, dummyProgramsForTimeSlot.toMutableStateList())
            startTimeToProgramListAvailabilityMap.put(timeSlot, BooleanArray(totalChannelCount))
              //  Log.d(TAG, "createTimeSlotBasedProgramLists for timeslot ${timeSlot}")
           // }
        }
    }

    private fun createDummyProgramList(){
        dummyProgramsForTimeSlot = ArrayList<ProgramsForTimeSlot>()
        for (i in 1..stationIds.size) {
            // create dummy GuideCell
            val guideCell = GuideCell(contentId = "Dummy", collectionId = "",
            contentType = ContentType.episode,
            duration = 1800,
            offerId= "",
            startTime = 0L,
            title = "Dummy")
            val guideCellList = ArrayList<GuideCell>()
            guideCellList.add(guideCell)
            dummyProgramsForTimeSlot.add(ProgramsForTimeSlot(guideCellList))
        }
    }



    private fun addProgramListForTimeStamp(
        guideRows: List<GuideRow>,
        timeSlot: Long
    ) {// TODO : further optimization of this function can be done by doing computation for 8 timeslots together
        Log.i(TAG, "addProgramListForTimeStamp  timeSlot $timeSlot")
        val programList = startTimeToProgramListMap.get(timeSlot)!!
        val programListAvailabilityStatus = startTimeToProgramListAvailabilityMap.get(timeSlot)!!
        for (rowNumber in 0..guideRows.size-1) {
            val cells = ArrayList<GuideCell>()
            for (cell in guideRows.get(rowNumber).guideCells) {
                //Log.i(TAG, "addProgramListForTimeStamp currentTime $timeSlot startTime ${cell.startTime} and duration ${cell.duration}")
                // both in second ?
                if (cell.startTime <= timeSlot &&
                    cell.startTime + cell.duration > timeSlot
                ) {
                    // program continuing from last time slot
                    cells.add(cell)
                } else if (cell.startTime >= timeSlot &&
                    cell.startTime < timeSlot + MainActivity.timeDurationPerPage
                ) {
                    // program starting in this time slot
                    cells.add(cell)
                }
            }
            // TODO :: Need to know if a station does not have guide cells data then how to make programListAvailabilityStatus true
            // TODO:: Can we avoid using programListAvailabilityStatus alltogether
            val rowNumberForStationId = stationIds.indexOf(guideRows.get(rowNumber).stationId)
            if(rowNumberForStationId >=0 && rowNumberForStationId < stationIds.size) {
                Log.i(TAG, "addProgramListForTimeStamp  rowNumberForStationId $rowNumberForStationId")
                programList.set(rowNumberForStationId, ProgramsForTimeSlot(cells))
                programListAvailabilityStatus[rowNumberForStationId] = true
            }
        }
       // printProgramList(lastCallStartTime)
    }

    private fun printProgramList(timeSlot: Long) {
        Log.i(TAG, "Printing for timeslot ${timeSlot}")
        val programList : SnapshotStateList<ProgramsForTimeSlot> = startTimeToProgramListMap.get(timeSlot)!!
        for (rowNumber in 0..programList.size-1){
            Log.i(TAG, "Printing for stationId ${lastCallStationIds.get(rowNumber)}")
            val programListForStation : ArrayList<GuideCell> = programList.get(rowNumber).programs
            for (cellNumber in 0..programListForStation.size-1){
                Log.i(TAG, "Content Id ${programListForStation.get(cellNumber).contentId}")
            }
        }
    }

    private fun generateRequestId(): String {

        val processId = Process.myPid()
        val threadId = Thread.currentThread().id
        val timestamp = System.nanoTime()

        return "$processId-$threadId-${++incrementCounter}-$timestamp"
    }

    data class GuideRowsAPICallingParams(
        val timeSlot: Long,
        val firstVisibleItemIndex: Int,
        val lastVisibleItemIndex: Int
    )
}