package com.solotodo.ui.onboarding.parts

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Renders every character of the input as uppercase. Needed for the Awakening
 * Step 2 / Step 3 designation input — we accept any case internally, but the
 * System voice is always uppercase, and pasted text must also uppercase.
 */
object UppercaseTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(
            text = AnnotatedString(text.text.uppercase()),
            offsetMapping = OffsetMapping.Identity,
        )
}
