#!/bin/bash

# execute all files with .py extension in this folder
find . -maxdepth 1 -name \*.py -exec python3 {} \;