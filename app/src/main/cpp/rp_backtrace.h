//
// Created by Tousif on 2020-01-09.
//

#ifndef RP_XPOSED_FRAMEWORK_RP_BACKTRACE_H
#define RP_XPOSED_FRAMEWORK_RP_BACKTRACE_H

#include <iostream>
#include <iomanip>

void dumpBacktrace(std::ostream& os, void** buffer, size_t count);
size_t captureBacktrace(void** buffer, size_t max);

extern "C" void printBackTrace(size_t count);

#endif //RP_XPOSED_FRAMEWORK_RP_BACKTRACE_H
