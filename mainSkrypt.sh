#!/usr/bin/env bash

lastPID=1

exitFunction() {
        while [[ "$(ps -e | grep "${lastPID}")" =~ '^(\d+)\ .+\ (\w+)$' ]]; do
			if [[ "${BASH_REMATCH[2]}" != 'python' ]]; then
				echo -e "\e[1m~\nExiting api script\e[0m"
				exit 0
			fi
			
			echo -e '\e[1m\nWaiting for curl...\e[0m'
			sleep 2s
		done
		
		echo -e "\e[1m\nExiting weather script\e[0m"
		exit 0
}

trap 'exitFunction' SIGINT

while :
do
	./skrypt.py &
	lastPID="$!"
	echo -e "$(date)~~~\n"
	sleep 10m
done
