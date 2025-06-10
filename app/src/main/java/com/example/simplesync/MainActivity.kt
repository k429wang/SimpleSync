package com.example.simplesync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.remember
import com.example.simplesync.ui.pages.MainScreen
import com.example.simplesync.ui.theme.SimpleSyncTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import com.example.simplesync.model.User
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import io.github.jan.supabase.postgrest.from
import androidx.compose.ui.unit.dp



val supabase = createSupabaseClient(
    supabaseUrl = "https://xugzrydvytilvvucphpg.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh1Z3pyeWR2eXRpbHZ2dWNwaHBnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDk0ODk2NTIsImV4cCI6MjA2NTA2NTY1Mn0.GPO7nHT7owWW02p2wzwTRn2e9C2RzBjM75Yfd2PgaAY"
) {
    install(Postgrest)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleSyncTheme {
//                MainScreen()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UsersList()
                }
                /*
                I'll build a custom Scaffold wrapper, it's just good practice.
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                */
            }
        }
    }
}

@Composable
fun UsersList() {
    var users by remember { mutableStateOf<List<User>>(listOf()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            users = supabase.from("users")
                .select().decodeList<User>()
        }
    }
    LazyColumn {
        items(
            users,
            key = { user -> user.userName },
        ) { user ->
            Text(
                text = "${user.userName} - ${user.email}: ${user.firstName} ${user.lastName}",
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SimpleSyncTheme {
        Greeting("Android")
    }
}