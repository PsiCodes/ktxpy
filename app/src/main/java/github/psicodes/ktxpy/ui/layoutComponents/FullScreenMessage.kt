package github.psicodes.ktxpy.ui.layoutComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.psicodes.ktxpy.R

@Composable
fun FullScreenMessage(
    icon: Painter,
    title: String,
    message: String
)
{
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily(Font(R.font.roboto_condensed_bold))
        )
        Text(
            text = message,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 15.sp,
            fontFamily = FontFamily(Font(R.font.roboto_condensed_bold)),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}