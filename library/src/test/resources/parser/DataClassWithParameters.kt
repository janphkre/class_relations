package com.example.a

import com.example.b.ParameterClassB
import com.example.b.ParameterClassC
import com.example.c.ParameterClassD
import io.other.ParameterClassE

data class DataClassWithParameters(
    private val parameterA: ParameterClassA,
    parameterB: ParameterClassB,
    var parameterC: ParameterClassC,
    internal val parameterD: ParameterClassD,
    val parameterE: ParameterClassE,
) {
}