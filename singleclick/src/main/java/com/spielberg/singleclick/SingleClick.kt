package com.spielberg.singleclick


@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@kotlin.annotation.Retention(
    AnnotationRetention.RUNTIME
)
annotation class SingleClick(
    val value: Int = 500,
    val except: IntArray = [],
    val exceptIdName: Array<String> = []
)