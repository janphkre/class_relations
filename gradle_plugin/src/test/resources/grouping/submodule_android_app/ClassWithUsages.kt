package multi_module_example.android.example.submodule_app

import multi_module_example.android.example.submodule_library.UsageClassC
import io.other.UsageClassE
import io.other.UsageClassF

class ClassWithUsages {

    fun methodA(param1: UsageClassE) {
    }
    private fun methodB(param2: (UsageClassF) -> Unit) {
        UsageClassC()
    }
    internal fun methodC() {
    }
}