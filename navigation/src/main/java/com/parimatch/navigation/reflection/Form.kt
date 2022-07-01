package com.parimatch.navigation.reflection

import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment as AndroidFragment

public sealed class Form {

	public data class Fragment(val fragment: AndroidFragment) : Form()

	public data class Compose(val composable: @Composable () -> Unit) : Form()
}
