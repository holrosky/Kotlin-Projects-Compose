package com.example.flomusicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.flomusicplayer.extension.toLyricList
import com.example.flomusicplayer.extension.toMinute
import com.example.flomusicplayer.extension.toTimeList
import com.example.flomusicplayer.ui.theme.FLOMusicPlayerTheme
import com.example.viewmodel.MusicViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

val LocalMusicViewModel = staticCompositionLocalOf<MusicViewModel> {
    error("No MusicViewModel provided")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val musicViewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(LocalMusicViewModel provides musicViewModel) {
                FLOMusicPlayerTheme {
                    MyApp()
                }
            }
        }
    }
}

@Composable
fun MyApp() {
    val splashScreenVisible = rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(splashScreenVisible) {
        delay(2000)
        splashScreenVisible.value = false
    }

    if (splashScreenVisible.value) {
        SplashScreen()
    } else {
        MainScreen()
    }
}

@Composable
fun MainScreen() {
    val musicViewModel = LocalMusicViewModel.current
    val music = musicViewModel.music.collectAsState().value
    val (isBoxClicked, setBoxClicked) = remember { mutableStateOf(false) }

    music?.let {

        if (isBoxClicked) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    LyricsScreen(music.lyrics.toLyricList(), music.lyrics.toTimeList(), setBoxClicked)
                }
                MusicPlayer()
            }

        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(30.dp))

                        TitleWIthSinger(music.title, music.singer)

                        Spacer(modifier = Modifier.height(50.dp))

                        AlbumImg(imageUrl = music.image)

                        Spacer(modifier = Modifier.height(50.dp))

                        LyricsTrack(music.lyrics.toLyricList(), setBoxClicked)
                    }
                }

                MusicPlayer()
            }
        }
    } ?:
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Center
    ) {
        Text(text = "네트워크 에러!")
    }
}

@Composable
fun TitleWIthSinger(title: String, singer: String) {
    Text(text = title,
        fontSize = 20.sp)

    Spacer(modifier = Modifier.height(10.dp))

    Text(text = singer,
        fontSize = 10.sp)
}

@Composable
fun AlbumImg(imageUrl: String) {
    Card(
        modifier = Modifier.size(300.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 15.dp
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun LyricsTrack(lyrics: List<String>, setBoxClicked: (Boolean) -> Unit) {
    val musicViewModel = LocalMusicViewModel.current
    val lyricsIndex by musicViewModel.currentLyricsIndex.collectAsState()

    Box(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { setBoxClicked(true) }
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = lyrics.getOrNull(lyricsIndex) ?: "",
                color = Color.Blue
            )
            Text(text = lyrics.getOrNull(lyricsIndex + 1) ?: "")
        }
    }
}

@Composable
fun LyricsScreen(lyrics: List<String>, lyricTime: List<Int>, setBoxClicked: (Boolean) -> Unit) {
    val musicViewModel = LocalMusicViewModel.current
    val lyricsIndex by musicViewModel.currentLyricsIndex.collectAsState()

    BackHandler(onBack = {
        setBoxClicked(false)
    })

    val scrollState = rememberLazyListState()

    LaunchedEffect(lyricsIndex) {
        val temp = if (lyricsIndex < 8) 0 else lyricsIndex - 8
        scrollState.animateScrollToItem(temp)
    }

    LazyColumn(
        state = scrollState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(lyrics) { index, lyric ->
            Text(
                text = lyric,
                fontSize = 16.sp,
                color = if (index == lyricsIndex) Color.Blue else Color.Unspecified,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            musicViewModel.seekTo(lyricTime[index].toLong())
                        }
                    ),
            )
        }
    }
}

@Composable
fun MusicPlayer() {
    val musicViewModel = LocalMusicViewModel.current
    val sliderPosition by musicViewModel.sliderPosition.collectAsState()
    val isPlaying by musicViewModel.isPlaying.collectAsState()
    Column(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text(text = sliderPosition.toMinute(),
            color = Color.Blue)
            Spacer(modifier = Modifier.weight(1f))
            musicViewModel.getDuration()?.toMinute()?.let { Text(text = it) }
        }

        Slider(
            value = sliderPosition.toFloat(),
            onValueChange = { newValue ->
                musicViewModel.onSliderValueChange(newValue)
                musicViewModel.updateIsAdjusting(true)
            },
            onValueChangeFinished = {
                musicViewModel.onSliderValueChangeFinished()
                musicViewModel.updateIsAdjusting(false)
            },
            valueRange = 0f..musicViewModel.getDuration()?.toFloat()!!
        )

        Image(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { musicViewModel.updateIsPlaying(!isPlaying) }
            ),
            contentScale = ContentScale.FillWidth,
            painter = if (isPlaying) painterResource(R.drawable.ic_baseline_pause_50) else painterResource(R.drawable.ic_baseline_play_arrow_50),
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SplashScreen() {
    Box {
        Image(
            contentScale = ContentScale.FillWidth,
            painter = painterResource(R.drawable.flo_splash),
            contentDescription = null
        )
    }
}