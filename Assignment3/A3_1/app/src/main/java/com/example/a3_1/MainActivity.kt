package com.example.a3_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    init {
        System.loadLibrary("matrixcalculator")
    }

    external fun addMatrices(dim: Int, a: DoubleArray, b: DoubleArray): DoubleArray
    external fun subtractMatrices(dim: Int, a: DoubleArray, b: DoubleArray): DoubleArray
    external fun multiplyMatrices(dim: Int, a: DoubleArray, b: DoubleArray): DoubleArray
    external fun divideMatrices(dim: Int, a: DoubleArray, b: DoubleArray): DoubleArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MatrixCalculatorScreen()
                }
            }
        }
    }

    @Composable
    fun MatrixCalculatorScreen() {
        var dimText by remember { mutableStateOf("") }
        var matAText by remember { mutableStateOf("") }
        var matBText by remember { mutableStateOf("") }
        var selectedOp by remember { mutableStateOf("Add") }
        var resultText by remember { mutableStateOf("") }
        val ops = listOf("Add", "Subtract", "Multiply", "Divide")

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = dimText,
                onValueChange = { dimText = it },
                label = { Text("Dimension (n)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = matAText,
                onValueChange = { matAText = it },
                label = { Text("Matrix A (row-wise, space/newline separated)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = matBText,
                onValueChange = { matBText = it },
                label = { Text("Matrix B (row-wise, space/newline separated)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }

            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text("Operation: $selectedOp")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ops.forEach { op ->
                        DropdownMenuItem(onClick = {
                            selectedOp = op
                            expanded = false
                        }) {
                            Text(op)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                val dim = dimText.toIntOrNull()
                if (dim == null || dim <= 0) {
                    resultText = "Enter valid dimension"
                    return@Button
                }

                val aArr = parseMatrix(matAText, dim)
                val bArr = parseMatrix(matBText, dim)

                if (aArr == null || bArr == null) {
                    resultText = "Invalid matrix input"
                    return@Button
                }

                val resArr = when (selectedOp) {
                    "Add" -> addMatrices(dim, aArr, bArr)
                    "Subtract" -> subtractMatrices(dim, aArr, bArr)
                    "Multiply" -> multiplyMatrices(dim, aArr, bArr)
                    "Divide" -> divideMatrices(dim, aArr, bArr)
                    else -> DoubleArray(0)
                }

                resultText = formatMatrix(resArr, dim)
            }) {
                Text("Calculate")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Result:")
            Text(resultText, modifier = Modifier.fillMaxWidth())
        }
    }

    private fun parseMatrix(input: String, dim: Int): DoubleArray? {
        val tokens = input.trim().split("\\s+".toRegex())
        if (tokens.size != dim * dim) return null
        return DoubleArray(dim * dim) { i -> tokens[i].toDoubleOrNull() ?: return null }
    }

    private fun formatMatrix(arr: DoubleArray, dim: Int): String {
        if (arr.size != dim * dim) return "Error"
        return buildString {
            for (i in 0 until dim) {
                for (j in 0 until dim) {
                    append(arr[i * dim + j]).append(' ')
                }
                append('\n')
            }
        }
    }
}
