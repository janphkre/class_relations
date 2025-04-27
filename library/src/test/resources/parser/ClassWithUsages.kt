package com.example.a

import com.example.b.UsageClassB
import com.example.b.UsageClassC
import io.other.UsageClassD
import io.other.UsageClassE

class ClassWithUsages {

    init {
        USageClassA()
    }

    fun methodA(param1: UsageClassE) {
        UsageClassB()
    }
    private fun methodB() {
        UsageClassC()
    }
    internal fun methodC() {
        UsageClassD()
    }
}