#!/usr/bin/env python3
import sys
import re
import subprocess

print("hint: run this script from root directory of the project");

def get_current_pom_version():
    return subprocess.run('mvn help:evaluate -Dexpression=project.version -q -DforceStdout', shell=True, capture_output=True).stdout.decode();

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

def only_version(string_arg):
    return split_string(string_arg, "[-]");

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

def build_string_type(string_arr):
    final_string:str = ''
    j:int = 0
    delimiter_dash:str= '-'
    for i in string_arr:
        final_string += i
        if (j == len(string_arr) - 1):
            break;
        final_string += delimiter_dash
        j += 1;
    return final_string



def build_string_version(string_arr):
    final_string:str = ''
    j:int = 0
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

def exec_mvn_command(final_string):
    print(final_string)
    subprocess.run(f"mvn -DnewVersion=\"{final_string}\" versions:set", shell=True);

def debug_print():
    print("Expected at least 2 arguments.", "Total arguments passed:", len(sys.argv))

def help_print():
    print("Invalid usage: [OPTION] [ARGUMENT]")

if (len(sys.argv) < 2):
    debug_print()
    help_print()
    quit()

version_current = ''

if (len(sys.argv) == 3):
    version_current = sys.argv[2]
else:
   version_current = get_current_pom_version()

if (len(version_current) == 0):
    print("Shell returned empty string")
    quit()
match sys.argv[1]:
    case "-m":
        exec_mvn_command(build_string_version(increment_version(version_current, 2)))
    case "-M":
        exec_mvn_command(increment_version(version_current, 1))
    case "-s":
        exec_mvn_command(build_string_type(release_type(version_current,1, 1)))
    case "-r":
        exec_mvn_command(build_string_type(release_type(version_current,1, 2)))
    case "-n":
        exec_mvn_command(only_version(version_current)[0])
    case "-h":
        help_print()
    case _:
        help_print()
