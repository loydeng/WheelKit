# name use upper camel case (pascal) to avoid conflict

##############################  test #################################
# ARGV = (ARGV0 ARGV1 ... ARGN), ARGC is the number of ARGV, ARGN is all of the overflow param
# test func

function(Var1)
    set(v1 11 PARENT_SCOPE) # 作用域扩大至函数调用方, 只会扩大一层
endfunction()

function(Var2)
    set(v2 22)
    Var3()
    LogI("Var2: v1=${v1}")
endfunction()

function(Var3)
    set(v3 33 PARENT_SCOPE)
    LogI("Var3: v1=${v1}")
endfunction()

function(TeatScope)
    LogI("v1=${v1}, v2=${v2}, v3=${v3}")
endfunction()
# Var1()
# Var2() #
# TeatScope() # 输出 Var3: v1=11 ; Var2: v1=11 ; v1=11, v2=, v3=
# 以将函数 function 改为 macro 并去掉 PARENT_SCOPE 指定, 输出  v1=11, v2=22, v3=33 . 宏观调用会原地展开, 变量可直接使用.

function(TestArgs)
    PrintArgs(666 888 223)
    PrintArgs(11 22)
endfunction()
function(PrintArgs p1 p2)
    LogI("p1=${p1}") # p1=666
    LogI("p2=${p2}") # p2=888
    LogI("ARGV0=${ARGV0}") # ARGV0=666
    LogI("ARGV1=${ARGV1}") # ARGV1=888
    LogI("ARGC=${ARGC}") # ARGC=3
    LogI("ARGN=${ARGN}") # ARGN=223
    LogI("ARGV=${ARGV}") # ARGV=666;888;223
    foreach (opt ${ARGN})
        LogI(opt=${opt})
    endforeach ()
endfunction()
##############################  test #################################

##############################  common #################################
function(SetGlobalVariable name vale)
    if( "${ARGV2}" STREQUAL "APPEND")
        set_property(GLOBAL APPEND PROPERTY ${name} ${vale})
    else()
        set_property(GLOBAL PROPERTY ${name} ${vale})
    endif()
endfunction()

function(GetGlobalVariable name ret)
    get_property(value GLOBAL PROPERTY ${name})
    set(${ret} ${value} PARENT_SCOPE)
endfunction()

function(Call method)
    cmake_language(CALL ${method} ${ARGN})
endfunction()

function(Eval codes)
    cmake_language(EVAL CODE ${codes})
endfunction()

##############################  common #################################

##############################  cmake version setting #################################

macro(ProjectEnv project_name project_version cxx_version)
    # PROJECT_VERSION 由 PROJECT_VERSION_MAJOR, PROJECT_VERSION_MINOR, PROJECT_VERSION_PATCH, and PROJECT_VERSION_TWEAK 4个组成
    ParseVersion(${project_version} version_major version_minor version_patch version_tweak)
    set(${project_name}_VERSION_MAJOR ${version_major})
    set(${project_name}_VERSION_MINOR ${version_minor})
    set(${project_name}_VERSION_PATCH ${version_patch})
    set(${project_name}_VERSION_TWEAK ${version_tweak})

    set(CMAKE_CXX_STANDARD ${cxx_version})
    set(CMAKE_CXX_STANDARD_REQUIRED ON)

    SetGlobalVariable(${project_name}_local_libs 0)
    SetGlobalVariable(${project_name}_system_libs 0)
endmacro()
##############################  cmake version setting #################################

##############################  log print #################################
function(LogI msg)
    message(STATUS, "--------------------------------LogI------------------------------>")
    message(STATUS, "${msg}")
    message(STATUS, "<-------------------------------LogI-------------------------------")
endfunction()

function(LogW msg)
    message(WARNING, "--------------------------------LogW------------------------------>")
    message(WARNING, "${msg}")
    message(WARNING, "<-------------------------------LogW-------------------------------")
endfunction()

# stop processing and generation
function(LogE msg)
    message(FATAL_ERROR, "--------------------------------LogE------------------------------>")
    message(FATAL_ERROR, "${msg}")
    message(FATAL_ERROR, "<-------------------------------LogE-------------------------------")
endfunction()

# continue processing, but skip generation
function(LogEC msg)
    message(SEND_ERROR, "--------------------------------LogEC------------------------------>")
    message(SEND_ERROR, "${msg}")
    message(SEND_ERROR, "<-------------------------------LogEC-------------------------------")
endfunction()

##############################  log print #################################

##############################  string operate #################################
function(ParseVersion version_string version_major version_minor version_patch version_tweak)
    Split(${version_string} "." ver_list)
    set(i 1)
    foreach (v ${ver_list})
        if (${i} EQUAL 1)
            set(${version_major} ${v} PARENT_SCOPE)
        elseif (${i} EQUAL 2)
            set(${version_minor} ${v} PARENT_SCOPE)
        elseif (${i} EQUAL 3)
            set(${version_patch} ${v} PARENT_SCOPE)
        elseif (${i} EQUAL 4)
            set(${version_tweak} ${v} PARENT_SCOPE)
        endif ()
        PlusOne(${i} i)
    endforeach ()
endfunction()

function(Split str divider ret)
    set(s ${str})
    string(FIND ${s} ${divider} index)
    while (NOT (${index} EQUAL -1))
        string(SUBSTRING ${s} 0 ${index} item)
        if (NOT ("${s}" STREQUAL ""))
            list(APPEND items ${item})
        endif ()
        PlusOne(${index} index)
        string(SUBSTRING ${s} ${index} -1 s)
        if ("${s}" STREQUAL "")
            break()
        endif ()
        string(FIND ${s} ${divider} index)
    endwhile ()
    if (NOT ("${s}" STREQUAL ""))
        list(APPEND items ${s})
    endif ()
    set(${ret} ${items} PARENT_SCOPE)
endfunction()

function(Replace src target replace ret)
    string(REPLACE "${target}" "${replace}" out "${src}")
    set(${ret} ${out} PARENT_SCOPE)
endfunction()

# the div is blank space, like special string be "1 2 3"
function(StringToList src div ret)
    Replace("${src}" "${div}" ";" out)
    set(${ret} ${out} PARENT_SCOPE)
endfunction()

function(ListToString src div ret)
    # list(JOIN <list> <glue> <out-var>)  # CMake need 3.12, glue is this div
    Replace("${src}" ";" "${div}" out)
    set(${ret} ${out} PARENT_SCOPE)
endfunction()
##############################  string operate #################################

##############################  list operate #################################
# get the index of target in container
function(ListIndexOf container target ret)
    list(LENGTH container len)
    if (${len} EQUAL 0)
        set(${ret} -1 PARENT_SCOPE)
    else ()
        list(FIND container ${target} index)
        set(${ret} ${index} PARENT_SCOPE)
    endif ()
endfunction()

# check to see that the container contains target
function(ListContains container target ret)
    ListIndexOf("${container}" ${target} index)
    if (${index} EQUAL -1)
        set(${ret} 0 PARENT_SCOPE)
    else ()
        set(${ret} 1 PARENT_SCOPE)
    endif ()
endfunction()

# get the next element of target
function(ListTargetNext container target ret)
    ListIndexOf("${container}" ${target} index)
    if (NOT (${index} EQUAL -1))
        PlusOne(${index} index)
        list(LENGTH container len)
        if (${index} LESS ${len})
            list(GET container ${index} v)
            set(${ret} ${v} PARENT_SCOPE)
        endif ()
    endif ()
endfunction()

# iterate list and call action with element
function(ListIterate container action)
    foreach (e ${container})
        Call(${action} ${e})
    endforeach (e)
endfunction()

##############################  list operate #################################

##############################  文件操作 #################################
function(IsFileExist filePath ret)
    if (EXISTS ${filePath})
        set(${ret} 1 PARENT_SCOPE)
    else ()
        set(${ret} 0 PARENT_SCOPE)
    endif ()
endfunction()

function(IsDir dirPath ret)
    if (IS_DIRECTORY ${dirPath})
        set(${ret} 1 PARENT_SCOPE)
    else ()
        set(${ret} 0 PARENT_SCOPE)
    endif ()
endfunction()

function(FileName filePath ret)
    IsFileExist(${filePath} check)
    if (NOT ${check})
        LogE("lib is not exist for filePath=${filePath}")
        set(${ret} "-1" PARENT_SCOPE)
        return()
    endif ()
    string(FIND ${filePath} "/" start REVERSE)
    if ("${start}" GREATER_EQUAL "0")
        PlusOne(${start} start)
        string(SUBSTRING ${filePath} ${start} -1 file_name)
        set(${ret} ${file_name} PARENT_SCOPE)
    else ()
        set(${ret} ${filePath} PARENT_SCOPE)
    endif ()
endfunction()

function(FileSuffix name ret)
    string(FIND ${name} "." start REVERSE)
    if ("${start}" GREATER_EQUAL "0")
        PlusOne(${start} start)
        string(SUBSTRING ${name} ${start} -1 suffix)
        set(${ret} ${suffix} PARENT_SCOPE)
    else ()
        set(${ret} "" PARENT_SCOPE)
    endif ()
endfunction()

function(LibName name ret)
    if ("${name}" MATCHES "lib[a-zA-z][A-Za-z0-9_]*\\.(so|a)")
        string(FIND ${name} "." end REVERSE)
        Minus(${end} 3 len)
        string(SUBSTRING ${name} 3 ${len} simple_name)
        set(${ret} ${simple_name} PARENT_SCOPE)
    else ()
        LogW("${name} is not match lib*.so or lib*.a!")
        set(${ret} "-1" PARENT_SCOPE)
    endif ()
endfunction()
##############################  文件操作 #################################

##############################  数学运算 #################################
function(Plus v1 v2 ret)
    math(EXPR v "${v1} + ${v2}")
    set(${ret} ${v} PARENT_SCOPE)
endfunction()

function(Minus v1 v2 ret)
    math(EXPR v "${v1} - ${v2}")
    set(${ret} ${v} PARENT_SCOPE)
endfunction()

function(Multiply v1 v2 ret)
    math(EXPR v "${v1} * ${v2}")
    set(${ret} ${v} PARENT_SCOPE)
endfunction()

function(Divide v1 v2 ret)
    math(EXPR v "${v1} / ${v2}")
    set(${ret} ${v} PARENT_SCOPE)
endfunction()

# 取模
# 与取余区别: 对正数操作的结果一致, 对负数操作的结果不同
# 取模 (Modulo Operation)    mod : -7 / 4 = ( 1 + (-2)*4) / 4 = -2..1（向负无穷方向舍入）
# 取余 (Remainder Operation) red : -7 / 4 = (-3 + (-1)*4) / 4 = -1..-3（向0方向舍入）
function(Mod v1 v2 ret)
    math(EXPR v "${v1} % ${v2}")
    set(${ret} ${v} PARENT_SCOPE)
endfunction()

function(PlusOne v1 ret)
    Plus(${v1} 1 v)
    set(${ret} ${v} PARENT_SCOPE)
endfunction()

function(MinusOne v1 ret)
    Minus(${v1} 1 v)
    set(${ret} ${v} PARENT_SCOPE)
endfunction()

##############################  数学运算 #################################

##############################  库加载相关 #################################
function(AddTargetLib type src)
    add_library(${PROJECT_NAME} ${type} ${src})
endfunction()

function(AddSystemLibs)
    foreach (name ${ARGV})
        find_library(sysroot_${name} ${name})
        GetGlobalVariable(${PROJECT_NAME}_system_libs system_libs)
        if ("${system_libs}" STREQUAL "0")
            SetGlobalVariable(${PROJECT_NAME}_system_libs "\${sysroot_${name}}")
        else ()
            SetGlobalVariable(${PROJECT_NAME}_system_libs "\${sysroot_${name}}" APPEND)
        endif ()
    endforeach (name)
endfunction()

# 获取指定目录下的库列表
# dir 指定目录
# ret 输出变量, 保存库列表
# 可选参数, 指定加载库类型, EXT so (动态库) 和 EXT a (静态库)
# 可选参数, 若包含子目录下的库, 指定参数 RECURSE
function(GetDirLibs dir ret)
    IsDir(${dir} check)
    if (NOT ${check})
        LogE("dir is invalid in ${dir}")
        return()
    endif ()

    if ("${ARGC}" GREATER_EQUAL "2")
        # 不含子目录, 使用GLOB; 包含子目录, 使用 GLOB_RECURSE
        ListContains("${ARGN}" RECURSE c)
        if (${c})
            set(scope GLOB_RECURSE)
        else ()
            set(scope GLOB)
        endif ()

        ListTargetNext("${ARGN}" EXT ext)
        if ("${ext}" STREQUAL "")
            FILE(${scope} so_files ${dir}/lib*.so)
            FILE(${scope} a_files ${dir}/lib*.a)
        elseif ("${ext}" STREQUAL "so")
            FILE(${scope} so_files ${dir}/lib*.so)
        elseif ("${ext}" STREQUAL "a")
            FILE(${scope} a_files ${dir}/lib*.a)
        endif ()
        list(APPEND files ${so_files} ${a_files})
        set(${ret} ${files} PARENT_SCOPE)
    else ()
        LogE("GetDirLibs has more than 2. param 2 is dir; params, param 1 is ret; opt param is EXT and RECURSE")
    endif ()
endfunction()


function(AddLocalLib libPath)
    IsFileExist(${libPath} check)
    if (NOT ${check})
        LogE("lib is not exist at ${path}")
        return()
    endif ()

    FileName(${libPath} name)
    LibName(${name} lib_name)
    if (NOT ("${lib_name}" EQUAL "-1"))
        FileSuffix(${name} ext_name)
        if ("${ext_name}" STREQUAL "so")
            add_library(${lib_name} SHARED IMPORTED)
        elseif ("${ext_name}" STREQUAL "a")
            add_library(${lib_name} STATIC IMPORTED)
        endif ()
        set_target_properties(${lib_name} PROPERTIES IMPORTED_LOCATION ${libPath})

        GetGlobalVariable(${PROJECT_NAME}_local_libs local_libs)
        if ("${local_libs}" STREQUAL "0")
            SetGlobalVariable(${PROJECT_NAME}_local_libs ${lib_name})
        else ()
            SetGlobalVariable(${PROJECT_NAME}_local_libs ${lib_name} APPEND)
        endif ()
    endif ()
endfunction()

# 加载指定目录下的所有库
# dir 指定目录
# 可选参数, 指定加载库类型, EXT so (动态库) 和 EXT a (静态库)
# 可选参数, 若包含子目录下的库, 指定参数 RECURSE
function(AddLocalLibs dir)
    GetDirLibs(${dir} lib_files ${ARGN})
    ListIterate("${lib_files}" AddLocalLib)
endfunction()

function(LinkLibsToTarget)
    GetGlobalVariable(${PROJECT_NAME}_local_libs local_libs)
    GetGlobalVariable(${PROJECT_NAME}_system_libs system_libs)
    list(REMOVE_DUPLICATES local_libs)
    list(REMOVE_DUPLICATES system_libs)
    list(APPEND all_libs ${PROJECT_NAME} ${local_libs} ${system_libs})
    LogI("${PROJECT_NAME}_local_libs=${local_libs}")
    LogI("${PROJECT_NAME}_system_libs=${system_libs}")
    Eval("target_link_libraries(${all_libs})")
endfunction()