import kotlin.random.Random

open class Vehiculo(
    val nombre: String,
    val marca: String,
    val modelo: String,
    val capacidadCombustible: Float,
    var combustibleActual: Float,
    var kilometrosActuales: Float
) {

    companion object {
        const val KM_POR_LITRO = 10
    }

    open fun obtenerInformacion(): String {
        val kilometrosARecorrer = combustibleActual * KM_POR_LITRO
        return "Puede recorrer $kilometrosARecorrer kilómetros"
    }

    open fun calcularAutonomia(): Float {
        return capacidadCombustible * KM_POR_LITRO
    }

    open fun realizarViaje(distancia: Float): Float {
        val distanciaMaxima = capacidadCombustible * KM_POR_LITRO
        val distanciaRealizable = if (distancia <= distanciaMaxima) {
            distancia
        } else {
            distanciaMaxima
        }
        val combustibleGastado = distanciaRealizable / KM_POR_LITRO
        combustibleActual -= combustibleGastado
        kilometrosActuales += distanciaRealizable
        return distanciaRealizable
    }


    open fun repostar(cantidad: Float): Float {
        if (cantidad <= 0 || cantidad + combustibleActual > capacidadCombustible) {
            combustibleActual = capacidadCombustible
        } else {
            combustibleActual += cantidad
        }
        return combustibleActual
    }

}

class Automovil(
    nombre: String,
    marca: String,
    modelo: String,
    capacidadCombustible: Float,
    combustibleActual: Float,
    kilometrosActuales: Float,
    val esHibrido: Boolean
) : Vehiculo(nombre, marca, modelo, capacidadCombustible, combustibleActual, kilometrosActuales) {

    companion object {
        const val KM_POR_LITRO = 15
    }

    fun realizaDerrape(): Float {
        val distanciaRecorrida = if (esHibrido) 6.25f else 7.5f
        val combustibleGastado = distanciaRecorrida / KM_POR_LITRO
        combustibleActual -= combustibleGastado
        return combustibleActual
    }

}

class Motocicleta(
    nombre: String,
    marca: String,
    modelo: String,
    capacidadCombustible: Float,
    combustibleActual: Float,
    kilometrosActuales: Float,
    val cilindrada: Int
) : Vehiculo(nombre, marca, modelo, capacidadCombustible, combustibleActual, kilometrosActuales) {

    companion object {
        const val KM_POR_LITRO = 20
    }

    fun realizaCaballito(): Float {
        val distanciaRecorrida = 6.5f
        val combustibleGastado = distanciaRecorrida / KM_POR_LITRO
        combustibleActual -= combustibleGastado
        return combustibleActual
    }

}

class Carrera(
    val nombreCarrera: String,
    val distanciaTotal: Float,
    val participantes: List<Vehiculo>
) {
    private var estadoCarrera: Boolean = false
    private val historialAcciones: MutableMap<String, MutableList<String>> = mutableMapOf()
    private val posiciones: MutableMap<String, Float> = mutableMapOf()
    private val repostajes: MutableMap<String, Int> = mutableMapOf()

    fun iniciarCarrera() {
        estadoCarrera = true
        while (!determinarGanador()) {
            for (participante in participantes) {
                avanzarVehiculo(participante)
            }
        }
        obtenerResultados().forEach { println(it) }
    }

    private fun avanzarVehiculo(vehiculo: Vehiculo) {
        if (estadoCarrera) {
            val distanciaRecorrida = Random.nextFloat() * 190 + 10
            val distanciaARecorrer = minOf(distanciaRecorrida, distanciaTotal - posiciones.getOrDefault(vehiculo.nombre, 0f))
            val distanciaRecorridaReal = vehiculo.realizarViaje(distanciaARecorrer)
            registrarAccion(vehiculo.nombre, "Avanzó $distanciaRecorridaReal kilómetros.")
            realizarFiligrana(vehiculo)
            actualizarPosiciones()
            if (necesitaRepostar(vehiculo)) {
                repostarVehiculo(vehiculo, vehiculo.capacidadCombustible - vehiculo.combustibleActual)
            }
        } else {
            println("La carrera ha finalizado.")
        }
    }

    private fun realizarFiligrana(vehiculo: Vehiculo) {
        if (Random.nextBoolean()) {
            if (vehiculo is Automovil) {
                vehiculo.realizaDerrape()
                registrarAccion(vehiculo.nombre, "Realizó un derrape.")
            } else if (vehiculo is Motocicleta) {
                vehiculo.realizaCaballito()
                registrarAccion(vehiculo.nombre, "Realizó un caballito.")
            }
        }
    }

    private fun repostarVehiculo(vehiculo: Vehiculo, cantidad: Float) {
        vehiculo.repostar(cantidad)
        val actualRepostajes = repostajes[vehiculo.nombre] ?: 0
        repostajes[vehiculo.nombre] = actualRepostajes + 1
        registrarAccion(vehiculo.nombre, "Repostó $cantidad litros de combustible.")
    }


    private fun actualizarPosiciones() {
        posiciones.clear()
        for (participante in participantes) {
            posiciones[participante.nombre] = participante.kilometrosActuales
        }
    }

    private fun determinarGanador(): Boolean {
        val ganadores = posiciones.filterValues { it >= distanciaTotal }
        if (ganadores.isNotEmpty()) {
            println("La carrera ha finalizado.")
            estadoCarrera = false
            return true
        }
        return false
    }

    private fun registrarAccion(vehiculo: String, accion: String) {
        historialAcciones.getOrPut(vehiculo, { mutableListOf() }).add(accion)
    }

    private fun necesitaRepostar(vehiculo: Vehiculo): Boolean {
        return vehiculo.combustibleActual < vehiculo.capacidadCombustible / 4
    }

    private fun obtenerResultados(): List<String> {
        val resultados = mutableListOf<String>()
        val clasificacion = posiciones.toList().sortedBy { (_, value) -> value }.reversed()
        var posicion = 1
        for ((vehiculo, distancia) in clasificacion) {
            val repostajesRealizados = repostajes.getOrDefault(vehiculo, 0)
            val historial = historialAcciones.getOrDefault(vehiculo, emptyList())
            resultados.add("$posicion. $vehiculo - Distancia: $distancia km, Repostajes: $repostajesRealizados, Historial: $historial")
            posicion++
        }
        return resultados
    }
}

fun main() {
    val aurora = Automovil("Aurora", "Seat", "Panda", 50f, 50f * 0.1f, 0f, true)
    val boreal = Automovil("Boreal", "BMW", "M8", 80f, 80f * 0.1f, 0f, false)
    val cefiro = Motocicleta("Céfiro", "Derbi", "Motoreta", 15f, 15f * 0.1f, 0f, 500)
    val dinamo = Automovil("Dinamo", "Cintroen", "Sor", 70f, 70f * 0.1f, 0f, true)
    val eclipse = Automovil("Eclipse", "Renault", "Espacio", 60f, 60f * 0.1f, 0f, false)
    val fenix = Motocicleta("Fénix", "Honda", "Vital", 20f, 20f * 0.1f, 0f, 250)

    val participantes = listOf(aurora, boreal, cefiro, dinamo, eclipse, fenix)
    val carrera = Carrera("Gran Premio", 1000f, participantes)
    carrera.iniciarCarrera()
}
