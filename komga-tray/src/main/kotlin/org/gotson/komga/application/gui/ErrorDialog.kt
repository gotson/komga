package org.gotson.komga.application.gui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.gotson.komga.RB
import org.jetbrains.compose.resources.decodeToSvgPainter
import org.springframework.core.io.ClassPathResource

@Preview
fun showErrorDialog(
  text: String,
  stackTrace: String? = null,
) {
  application {
    Window(
      title = RB.getString("dialog_error.title"),
      onCloseRequest = ::exitApplication,
      visible = true,
      resizable = false,
      state =
        WindowState(
          placement = WindowPlacement.Floating,
          position = WindowPosition(alignment = Alignment.Center),
          size =
            DpSize(
              if (stackTrace != null) 800.dp else Dp.Unspecified,
              Dp.Unspecified,
            ),
        ),
      icon = ClassPathResource("icons/komga-color.svg").inputStream.readAllBytes().decodeToSvgPainter(LocalDensity.current),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(bottom = 16.dp),
        ) {
          Image(
            painter = ClassPathResource("icons/komga-color.svg").inputStream.readAllBytes().decodeToSvgPainter(LocalDensity.current),
            contentDescription = "Komga logo",
            modifier =
              Modifier
                .size(96.dp)
                .align(Alignment.Top),
          )
          Text(
            text,
            modifier = Modifier.padding(start = 32.dp),
          )
        }
        if (stackTrace != null)
          TextField(
            value = stackTrace,
            onValueChange = {},
            singleLine = false,
            maxLines = 15,
            modifier = Modifier.fillMaxWidth(),
          )

        Row(
          horizontalArrangement = if (stackTrace != null) Arrangement.SpaceBetween else Arrangement.End,
          modifier =
            if (stackTrace != null)
              Modifier.align(Alignment.End).fillMaxWidth()
            else
              Modifier.align(Alignment.End),
        ) {
          if (stackTrace != null) {
            val clipboardManager = LocalClipboardManager.current
            TextButton(
              onClick = {
                clipboardManager.setText(AnnotatedString(stackTrace))
              },
            ) {
              Text(RB.getString("dialog_error.copy_clipboard"))
            }
          }
          Button(
            onClick = { exitApplication() },
          ) {
            Text(RB.getString("dialog_error.close"))
          }
        }
      }
    }
  }
}
