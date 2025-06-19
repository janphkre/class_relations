package com.example.a

import com.example.b.ParameterClassA
import com.example.b.ParameterClassB
import com.example.c.ParameterClassB as ParameterClassC

class ClassWithAliasDeps(
    private val parameterA: ParameterClassA,
    parameterB: ParameterClassB,
    var parameterC: ParameterClassC,
) {
}