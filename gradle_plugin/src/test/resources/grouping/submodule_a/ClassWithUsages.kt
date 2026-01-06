package multi_module_example.submodule_a

import multi_module_example.submodule_b.UsageClassC
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