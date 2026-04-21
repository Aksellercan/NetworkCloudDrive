#!/usr/bin/env python3
import sys
import re

def enumerator(choice:int):
    match choice:
        case 0:
            return ""
        case 1:
            return "SNAPSHOT"
        case 2:
            return "RELEASE"
        case _:
            return ""

def split_string(string_to_split:str, regex:str):
   return re.split(regex, string_to_split)

def increment_version(argument:str, offset:int):
    result = split_string(argument, "[.-]")
    j:int = 0;
    for i in result:
        if (j == offset):
            num:int = int(i)
            num += 1
            result[j]:str = str(num)
        j += 1
    return result

def release_type(argument:str, offset:int, release_type:int):
    result = split_string(argument, "[-]")
    print(result)
    j:int = 0;
    for i in result:
        if (j == offset):
            result[j] = enumerator(release_type)
        j += 1
    return result


def build_string(string_arr):
    final_string:str = ''
    j:num = 0
    delimiter_dot:str= '.'
    delimiter_dash:str= '-'

    for i in string_arr:
        final_string += i
        if (j == len(string_arr) - 1):
            break;
        if (j < 2):
            final_string += delimiter_dot
        else:
            final_string += delimiter_dash
        j += 1
    return final_string

def debug_print():
    print("Expected 3 arguments.", "Total arguments given:", len(sys.argv))

def help_print():
    print("Invalid usage: [OPTION] [ARGUMENT]")

if (len(sys.argv) < 3):
    debug_print()
    help_print()
    quit()

match sys.argv[1]:
    case "-m":
        print(build_string(increment_version(sys.argv[2], 2)))
    case "-M":
        print(increment_version(sys.argv[2], 1))
    case "-s":
        print(build_string(release_type(sys.argv[2],1, 1)))
    case "-r":
        print(build_string(release_type(sys.argv[2],1, 2)))
    case _:
        help_print()
