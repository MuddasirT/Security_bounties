cmake_minimum_required(VERSION 3.22.1)
project(andy_llama LANGUAGES C CXX)

set(ANDROID_STL c++_shared)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_CURL OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

include(FetchContent)
FetchContent_Declare(
    llama_cpp
    GIT_REPOSITORY https://github.com/ggml-org/llama.cpp.git
    GIT_TAG b12556
    GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(llama_cpp)

add_library(andy-llama SHARED native-lib.cpp)
target_compile_features(andy-llama PRIVATE cxx_std_17)
target_compile_options(andy-llama PRIVATE -O3 -fvisibility=hidden)
target_link_libraries(andy-llama PRIVATE llama android log)
