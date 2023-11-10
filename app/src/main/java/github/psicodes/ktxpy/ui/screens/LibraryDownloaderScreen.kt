package github.psicodes.ktxpy.ui.screens

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import github.psicodes.ktxpy.R
import github.psicodes.ktxpy.ui.layoutComponents.FullScreenMessage

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun LibraryDownloaderScreen()
{
    Scaffold(
        topBar =
        {
            Surface(shadowElevation = 10.dp){
                TopAppBar(
                    title = {
                        Text(
                            text = "Libraries",
                            fontFamily = FontFamily(Font(resId = R.font.roboto_condensed_bold))
                        )
                    }
                )
            }
        }
    ) {
        FullScreenMessage(
            icon = painterResource(id = R.drawable.coming_soon_icon),
            title = "Coming Soon",
            message = "Libraries like numpy, pandas, matplotlib, etc will be available soon."
        )
    }
}