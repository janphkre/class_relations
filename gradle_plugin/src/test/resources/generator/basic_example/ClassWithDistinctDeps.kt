package basic_example

import com.example.b.ParameterClassB
import com.example.b.ParameterClassB
import com.example.b.ParameterClassC
import com.example.c.ParameterClassD
import com.example.c.ParameterClassD
import com.other.ParameterClassE

class ClassWithDistinctDeps(
    private val parameterA1: ParameterClassA,
    private val parameterA2: ParameterClassA,
    parameterB: ParameterClassB,
    var parameterC1: ParameterClassC,
    var parameterC2: ParameterClassC,
    internal val parameterD: ParameterClassD,
    val parameterE: ParameterClassE,
): ParameterClassB {
}