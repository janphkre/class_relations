package com.example.a

import com.other.ParameterClassA
import io.other.ParameterClassB

interface InterfaceWithUsages: ParameterClassB {
    fun methodA(parameter1: ParameterClassA)
    fun methodB() { }
    fun methodC()
    fun methodD()
}