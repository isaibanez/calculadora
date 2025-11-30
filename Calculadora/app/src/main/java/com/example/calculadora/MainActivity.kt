// Actividad 1 - 1er Trimestre - Programación Multimedia
// Isabel Ibáñez Seoane - 2º DAM


package com.example.calculadora

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.calculadora.databinding.ActivityMainBinding
import kotlin.math.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    // Variables de control para validar si se puede añadir una operación o un decimal (,)
    private var puedeAnadirOperacion = false
    private var puedeAnadirDecimales = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        acciones()
    }

    // Función para añadir un número o una coma al TextView de operación (textoOperacion)
    // numero = un número o una coma
    // Solo permitir añadir coma si:
    // 1. No hay otra coma en el número actual (puedeAnadirDecimales = true)
    // 2. TextView no está vacío (no puede empezar con una coma)
    private fun anadirNumero(numero: String){
        if(numero == ","){
            if(puedeAnadirDecimales && binding.textoOperacion?.text?.isNotEmpty() == true){
                binding.textoOperacion?.append(numero)
                puedeAnadirDecimales = false // No se pueden añadir decimales hasta próximo operador
                puedeAnadirOperacion = false // No permite añadir un operador justo después de una coma
            }
        } else{
            // Si el número es otro, se añade
            binding.textoOperacion?.append(numero)
            puedeAnadirOperacion = true // Después de añadir número se puede añadir operador
        }

    }

    // Función para añadir un operador (+, -, x, /)
    // operacion = un operador
    // Solo añadir un operador después de un número, nunca después de otro operador
    private fun anadirOperacion(operacion: String){
        if(puedeAnadirOperacion){
            binding.textoOperacion?.append(operacion)
            puedeAnadirOperacion = false // No se pueden añadir más operadores hasta el siguiente número
            puedeAnadirDecimales = true // Se puede añadir una coma en el siguiente número
        }
    }

    // Función para hacer el cálculo matemático
    // Calcula de izquierda a derecha
    // Ejemplo: 2 x 3 = 6 --> 2 x 3 + 5 (añadimos el +5, se lo suma al resultado 6 = 11, y así sucesivamente)
    private fun calcularResultado(){
        // Saca el texto de la operación
        val expresion = binding.textoOperacion?.text.toString()

        // Si está vacío, salimos
        if(expresion.isEmpty()){
            return
        }

        try {
            // Reemplaza la coma por un punto para poder hacer las operaciones matemáticas
            val expresionNormalizada = expresion.replace(",", ".")

            // Listas para almacenar números y operadores por separado
            val numeros = mutableListOf<Double>() // Ejemplo: [5.0. 4.0, 5.5]
            val operadores = mutableListOf<String>() // Ejemplo: ["+", "x"]

            // Variable para ir construyendo cada número carácter por carácter
            var numeroActual = ""

            // Recorre cada carácter de la expresión ya normalizada (con las comas cambiadas)
            for(caracter in expresionNormalizada) {
                // Si el carácter es un dígito o una coma, lo añade al número actual
                if(caracter.isDigit() || caracter == '.') {
                    numeroActual += caracter
                    // Si es un operador, significa que ha terminado el número
                    // Se convierte el texto a número decimal y se guarda
                    // Se deja la variable vacía para empezar con el siguiente número
                } else if(caracter in listOf('+', '-', 'x', '/')) {
                    if(numeroActual.isNotEmpty()) {
                        numeros.add(numeroActual.toDouble())
                        numeroActual = ""
                    }
                    // Se guarda el operador
                    operadores.add(caracter.toString())
                }
            }

            // Si no hay operador después del último número, se guarda
            if(numeroActual.isNotEmpty()) {
                numeros.add(numeroActual.toDouble())
            }

            // Validar que los datos son válidos para hacer el cálculo
            // N números = N-1 operadores
            // Ejemplo: 5 + 3 --> 2 números, 1 operador = OK
            if(numeros.isEmpty() || numeros.size != operadores.size + 1) {
                return
            }

            // Comienza con el primer número como primer resultado
            var resultado = numeros[0]

            // Aplicar cada operador
            for(i in operadores.indices){
                val operador = operadores[i] // Mete el operador actual
                val siguienteNumero = numeros[i + 1] // Mete el siguiente número

                // Dependiendo del operador, lo aplica
                resultado = when(operador) {
                    "+" -> resultado + siguienteNumero
                    "-" -> resultado - siguienteNumero
                    "x" -> resultado * siguienteNumero
                    "/" -> {
                        // En el caso de la división, devuelve un error si se intenta dividir por cero
                        if(siguienteNumero == 0.0) {
                            binding.textoResultados?.text = "Error: División por 0"
                            return
                        }
                        resultado / siguienteNumero
                    }
                    else -> resultado // Si no se reconoce el operador, se queda con el último resultado
                }
            }

            // Formatear resultado, si es entero quitar decimales
            val resultadoFormateado = if(resultado % 1.0 == 0.0){
                resultado.toInt().toString()
            } else {
                // Cambia el punto por la coma
                resultado.toString().replace(".", ",")
            }

            // Mostrar el resultado formateado en el TextView
            binding.textoResultados?.text = resultadoFormateado

        } catch(e: Exception) {
            // Si hay cualquier error, mostrar mensaje de error
            binding.textoResultados?.text = "Error"
        }
    }

    // Función para los cálculos de las operaciones unarias (seno, coseno, tangente, porcentaje, raíz cuadrada)
    // Primero calcular si hay alguna operación pendiente (ejemplo: 5 + 3 sin calcular, primero calcula el 8)
    private fun calcularResultadoOperacionUnaria(operacion: String) {
        calcularResultado()

        // Si hay un resultado en pantalla, usar valor del resultado
        // Si no hay resultado en pantalla, usar valor de la operación
        val valorActual = if(binding.textoResultados?.text?.isNotEmpty() == true){
            binding.textoResultados?.text.toString()
        } else {
            binding.textoOperacion?.text.toString()
        }

        // Si está vacío, no hacer nada
        if(valorActual.isEmpty()) {
            return
        }

        try {
            // Cambiar coma por punto y convertir texto a número
            val valor = valorActual.replace(",", ".").toDouble()

            // Aplicar la operación correspondiente
            val resultado = when(operacion) {
                "%" -> valor / 100.0
                "sin" -> sin(Math.toRadians(valor))
                "cos" -> cos(Math.toRadians(valor))
                "tan" -> tan((Math.toRadians(valor)))
                "√" -> sqrt(valor)
                else -> valor // Si no reconoce operador, mostrar el mismo valor
            }

            val resultadoFormateado = if(resultado % 1.0 == 0.0){
                resultado.toInt().toString()
            } else {
                // Si el resultado tiene decimales:
                // Formatear para mostrar 8 decimales como máximo
                // Eliminar ceros al final
                // Eliminar punto si quedó al final
                // Reemplazar punto por coma
                String.format("%.8f", resultado)
                    .trimEnd('0')
                    .trimEnd('.')
                    .replace(".", ",")
            }

            // Mostrar resultado formateado
            binding.textoResultados?.text = resultadoFormateado

            // Mostrar en el texto de la operación lo que se ha calculado
            // Ejemplo: "sin(8)"
            binding.textoOperacion?.text = when(operacion) {
                "%" -> "$valorActual%"
                "sin" -> "sin($valorActual)"
                "cos" -> "cos($valorActual)"
                "tan" -> "tan($valorActual)"
                "√" -> "√($valorActual)"
                else -> valorActual
            }

            // Permitir seguir operando
            puedeAnadirDecimales = true
            puedeAnadirOperacion = true

            // Si hay algún error, mostrar un mensaje de error
        } catch(e: Exception) {
            binding.textoResultados?.text = "Error"
        }

    }


    private fun acciones() {
        binding.btnLimpiar?.setOnClickListener(this)
        binding.btnBorrar?.setOnClickListener(this)
        binding.textoOperacion?.setOnClickListener(this)

        // Botones de números
        binding.btn0?.setOnClickListener(this)
        binding.btn1?.setOnClickListener(this)
        binding.btn2?.setOnClickListener(this)
        binding.btn3?.setOnClickListener(this)
        binding.btn4?.setOnClickListener(this)
        binding.btn5?.setOnClickListener(this)
        binding.btn6?.setOnClickListener(this)
        binding.btn7?.setOnClickListener(this)
        binding.btn8?.setOnClickListener(this)
        binding.btn9?.setOnClickListener(this)

        binding.btnComa?.setOnClickListener(this)

        // Botones de operación
        binding.btnSumar?.setOnClickListener(this)
        binding.btnRestar?.setOnClickListener(this)
        binding.btnDividir?.setOnClickListener(this)
        binding.btnMultiplicar?.setOnClickListener(this)
        binding.btnPorcentaje?.setOnClickListener(this)
        binding.btnIgual?.setOnClickListener(this)

        // Botones de operación científica (horizontal)
        binding.btnRaizCuadrada?.setOnClickListener(this)
        binding.btnSin?.setOnClickListener(this)
        binding.btnCos?.setOnClickListener(this)
        binding.btnTan?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when(v?.id){
            // Botón para limpiar el texto, tanto de la operación como del resultado
            // Se actualizan los flags para poder volver a meter decimales y operación tras el número
            binding.btnLimpiar?.id -> {
                binding.textoOperacion?.text=""
                binding.textoResultados?.text=""
                puedeAnadirOperacion = false
                puedeAnadirDecimales = true
            }

            // Botón para borrar el último carácter de la operación
            binding.btnBorrar?.id -> {
                val textoOperacionActual = binding.textoOperacion?.text.toString()
                val longitud = textoOperacionActual.length

                if(longitud > 0){
                    // Guardar carácter que se va a borrar
                    val caracterBorrado = textoOperacionActual.last().toString()

                    // Crear nueva cadena sin el último carácter
                    val nuevaOperacion = textoOperacionActual.subSequence(0, longitud -1).toString()
                    binding.textoOperacion?.text = nuevaOperacion

                    // Si se borra una coma, permitir añadir otra
                    if(caracterBorrado == ",") {
                        puedeAnadirDecimales = true
                    }

                    // Actualizar el flag para añadir una nueva operación
                    if(nuevaOperacion.isNotEmpty()) {
                        // Si el último carácter es un operador, no permitir añadir otro operador
                        if(nuevaOperacion.last().toString().matches(Regex("[+\\-x/]"))) {
                            puedeAnadirOperacion = false
                        } else {
                            puedeAnadirOperacion = true
                        }
                    } else {
                        // Si se queda vacío, resetear los flags
                        puedeAnadirOperacion = false
                        puedeAnadirDecimales = true
                    }

                }
            }

            // Botón para calcular el resultado
            binding.btnIgual?.id -> {
                calcularResultado()
            }

            // Botón para calcular el porcentaje
            binding.btnPorcentaje?.id -> {
                calcularResultadoOperacionUnaria("%")
            }

            // Botón para calcular la raíz cuadrada
            binding.btnRaizCuadrada?.id -> {
                calcularResultadoOperacionUnaria("√")
            }

            // Botón para calcular el seno (en grados)
            binding.btnSin?.id -> {
                calcularResultadoOperacionUnaria("sin")
            }

            // Botón para calcular el coseno (en grados)
            binding.btnCos?.id -> {
                calcularResultadoOperacionUnaria("cos")
            }

            // Botón para calcular la tangente (en grados)
            binding.btnTan?.id -> {
                calcularResultadoOperacionUnaria("tan")
            }

        }

        // Reconocer la vista como un botón, así se accede a su .text y se puede sacar el texto para reconocer qué botón es
        val btnPulsado = v as? android.widget.Button
        // Obtener botón pulsado y su texto
        val textoBtn = btnPulsado?.text.toString()

        // Verificar qué tipo de botón se ha pulsado en base a su texto
        if(textoBtn.matches(Regex("[0-9,]"))) {
            anadirNumero(textoBtn)
        } else if(textoBtn.matches(Regex("[+\\-x/]"))) {
            anadirOperacion(textoBtn)
        } else if(v?.id == binding.btnIgual?.id) {
            calcularResultado()
        }

    }
}