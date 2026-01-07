package multi_module_example.android.example.submodule_library

import io.other.UsageClassD
import multi_module_example.android.example.submodule_library.subpackage.UsageClassE

interface UsageClassC {
    fun methodA(param1: UsageClassD, param2: UsageClassE)
}