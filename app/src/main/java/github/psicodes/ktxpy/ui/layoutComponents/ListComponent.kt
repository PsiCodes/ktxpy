package github.psicodes.ktxpy.ui.layoutComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.psicodes.ktxpy.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ListComponent(
    modifier: Modifier = Modifier,
    file : File ,
    icon : Int = R.drawable.python_icon
){
    Row(
        modifier=modifier
    )
    {
        Icon(painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .size(45.dp)
                .padding(5.dp),
        )
        Column(){
            Text(
                "Name : ${file.name}",
                fontFamily = FontFamily(Font(resId = R.font.custom_sans)),
                modifier = Modifier.padding(top=2.dp),
                maxLines = 1,
                fontSize = 12.sp
            )
            Text(
                text = "Last Modified : ${
                    SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(Date(file.lastModified()))
                }",
                maxLines = 1,
                fontFamily = FontFamily(Font(resId = R.font.custom_sans)),
                fontSize = 10.sp,
            )
            Text(
                text = "Size : ${
                    if (file.length() > 1024 * 1024)
                        "${file.length() / (1024 * 1024)} MB"
                    else
                        "${file.length() / 1024} KB"
                }",
                maxLines = 1,
                fontFamily = FontFamily(Font(resId = R.font.custom_sans)),
                fontSize = 10.sp,
            )
        }
    }

}