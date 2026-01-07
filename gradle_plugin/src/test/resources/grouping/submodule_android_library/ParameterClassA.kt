package multi_module_example.android.example.submodule_library

import com.other.ParameterClassE
import multi_module_example.android.example.submodule_library.subpackage.ParameterClassB

class ParameterClassA(
    private val param1: ParameterClassB
): ParameterClassE {
}