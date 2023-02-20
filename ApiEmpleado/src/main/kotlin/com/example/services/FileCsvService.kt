package com.example.services

import com.example.models.Empleado
import org.koin.core.annotation.Single
import java.io.File

@Single
class FileCsvService {
    private val empleados = mutableMapOf(
        1L to Empleado(id = 1L, name = "Dani", available = true),
        2L to Empleado(id = 2L, name = "Nuria", available = false),
        3L to Empleado(id = 3L, name = "Aura", available = true)
    )
    private val dir: String = System.getProperty("user.dir") + File.separator + "data"
    private val file: String = dir + File.separator + "data.csv"

    init {
        if (!File(dir).exists()) {
            File(dir).mkdir()
            File(file).createNewFile()
            writeFile(empleados)
        }
    }

    fun writeFile(empleados: MutableMap<Long, Empleado>) {
        File(file).bufferedWriter().use {
            it.write("id;uuid;name;available")
            empleados.forEach { key ->
                it.append("\n" + key.value.toString(";"))
            }
        }
    }

    fun writeEmpleado(empleado: Empleado) {
        File(file).appendText("\n" + empleado.toString(";"))
    }

    fun readFile(): MutableMap<Long, Empleado> {
        val empleados = mutableMapOf<Long, Empleado>()
        File(file).readLines().drop(1).map { it.split(";") }
            .map { field ->
                val empleado = Empleado(
                    field[0].toLong(),
                    field[1],
                    field[2],
                    field[3].toBoolean()
                )
                empleados.put(empleado.id!!, empleado)
            }
        return empleados
    }
}