package com.lumina.tvaggregator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.lumina.tvaggregator.navigation.TVAggregatorNavigation
import com.lumina.tvaggregator.ui.theme.TVAggregatorTheme
import com.lumina.tvaggregator.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TVAggregatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TVAggregatorNavigation(homeViewModel = homeViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh platform installation status when app resumes
        homeViewModel.refreshContent()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TVAggregatorTheme {
        // Preview content can be added here if needed
    }
}