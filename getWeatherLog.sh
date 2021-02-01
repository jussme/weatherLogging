#!/usr/bin/env bash

DATE=$(date +'%H%M_%d%m%Y')

echo $(scp -r remote@weatherServer:/home/remote/weather/log.txt ./"${DATE}.txt")

if [[ -e ./"${DATE}.txt" ]]; then
	echo -e "\e[1mSuccess\e[0m"
else
	echo -e "\e[1mFailure\e[0m"
fi
