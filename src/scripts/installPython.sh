#!/bin/bash

# login as root
sudo -s
# enter your pwd

# install python3, pip, and venv (virtual environment)
sudo apt install python3
sudp apt install python3-pip
sudo apt install python3-venv

# ================================= DEPENDENCIES ====================================
# Pandas: Python package, providing fast, flexible, and expressive data structures
pip install pandas


# ================================= GETTING STARTED ====================================
# cd /path/to/your/directory
# create a virtual env for your python project in selected directory
# this step is optional, but a good practice
python3 -m venv .venv

# activate previously created virtual env in the directory
source .venv/bin/activate
