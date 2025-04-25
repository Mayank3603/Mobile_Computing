cmake_minimum_required(VERSION 3.10.2)
project("a3_1")

add_library(
        matrixcalculator
        SHARED
        MatrixCalculator.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        matrixcalculator
        ${log-lib}
)