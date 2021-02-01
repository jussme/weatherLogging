#!/usr/bin/env bash
#let it=144
#CRNT=''

exitFunction() {
        while [ $(lsof | grep log.txt)!=0 ]; do
		sleep 2s
		echo -e '\e[1mLOOPING\e[0m'
	done
        echo -e "\e[1mExiting api script\e[0m"
        exit 0
}

trap 'exitFunction' SIGINT

while :
do
	./skrypt.sh
	#CRNT=$!
	echo $(date)
	sleep 10m
	#let --it
done
