package multi_module_example.android.example.submodule_app

import multi_module_example.android.example.submodule_library.ParameterClassA
import com.other.ParameterClassE

class ClassWithDistinctDeps(
    private val parameterA1: ParameterClassA,
    val parameterE: ParameterClassE,
) {
}