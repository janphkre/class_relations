package multi_module_example.submodule_a

import multi_module_example.submodule_b.ParameterClassA
import com.other.ParameterClassE

class ClassWithDistinctDeps(
    private val parameterA1: ParameterClassA,
    val parameterE: ParameterClassE,
) {
}