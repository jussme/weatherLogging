#!/usr/bin/env bash
KEY_WEATHERBIT=$(sed -n '1p' apiKeys.txt | awk '{print $2}')
KEY_OPENWEATHERMAP=$(sed -n '2p' apiKeys.txt | awk '{print $2}')
LAT=$(sed -n '2p' coords.txt | awk '{print $2}')
LON=$(sed -n '1p' coords.txt | awk '{print $2}')
REGEX_WEATHERBIT='.*"rh".*"pres":.*"ts":.*"temp":.*'
REGEX_OPENWEATHER='.*"temp":.*"pressure":.*"humidity":.*"dt":.*'

RESPONSE=$(curl "http://api.weatherbit.io/v2.0/current?lat="${LAT}"&lon="${LON}"&key="${KEY_WEATHERBIT}"")
! [[ ${RESPONSE} =~ ${REGEX_WEATHERBIT} ]] && exit 1
REGEX_PRES='("pres":)([0-9.]+)'
PRES=''
REGEX_TEMP='("temp":)([0-9.-]+)'
TEMP=''
REGEX_HUM='("rh":)([0-9.]+)'
HUM=''
REGEX_OBTIME='("ts":)([0-9.]+)'
OBTIME=''
if [[ "$RESPONSE" =~ $REGEX_PRES ]]; then
	PRES=${BASH_REMATCH[2]}
fi
if [[ "${RESPONSE}" =~ ${REGEX_TEMP} ]]; then
	TEMP="${BASH_REMATCH[2]}"
fi
if [[ "${RESPONSE}" =~ ${REGEX_HUM} ]]; then
	HUM="${BASH_REMATCH[2]}"
fi
if [[ "${RESPONSE}" =~ ${REGEX_OBTIME} ]]; then
	OBTIME="${BASH_REMATCH[2]}"
fi

RESPONSE=$(curl "https://api.openweathermap.org/data/2.5/weather?lat="${LAT}"&lon="${LON}"&appid="${KEY_OPENWEATHERMAP}"")
! [[ ${RESPONSE} =~ ${REGEX_OPENWEATHER} ]] && exit 1
REGEX_PRES='("pressure":)([0-9.]+)'
PRES2=''
REGEX_TEMP='("temp":)([0-9.-]+)'
TEMP2=''
REGEX_HUM='("humidity":)([0-9.]+)'
HUM2=''
REGEX_OBTIME='("dt":)([0-9.]+)'
OBTIME2=''
if [[ "$RESPONSE" =~ $REGEX_PRES ]]; then
	PRES2=${BASH_REMATCH[2]}
fi
if [[ "${RESPONSE}" =~ ${REGEX_TEMP} ]]; then
	TEMP2="${BASH_REMATCH[2]}"
fi
if [[ "${RESPONSE}" =~ ${REGEX_HUM} ]]; then
	HUM2="${BASH_REMATCH[2]}"
fi
if [[ "${RESPONSE}" =~ ${REGEX_OBTIME} ]]; then
	OBTIME2="${BASH_REMATCH[2]}"
fi

TEMP2=$(python -c "print(round($TEMP2-273.15, 2))")
#TEMP2=$(echo "$TEMP2-273.15" | bc)

dataArrayNames=("PRES" "TEMP" "HUM" "OBTIME" "PRES2" "TEMP2" "HUM2" "OBTIME2")
dataArray=("$PRES" "$TEMP" "$HUM" "$OBTIME" "$PRES2" "$TEMP2" "$HUM2" "$OBTIME2")
for i in ${!dataArray[@]}; do
	if [[ -z ${dataArray[$i]} ]]; then
		TIME=${OBTIME}
		if [[ $i < 3 ]]; then
			TIME=${OBTIME2}
		fi
		echo -e "$(date)\t${TIME}\t${dataArrayNames[$i]} = ${dataArray[$i]}\r" >> errLog.txt
		dataArray[$i]='NaN'
	else
		if [ $i != 3 ] && [ $i != 7 ]; then
                	dataArray[$i]=$(python -c "print(round(${dataArray[$i]}, 2))")
		fi
	fi
done

PRES=${dataArray[0]}
TEMP=${dataArray[1]}
HUM=${dataArray[2]}
OBTIME=${dataArray[3]}
PRES2=${dataArray[4]}
TEMP2=${dataArray[5]}
HUM2=${dataArray[6]}
OBTIME2=${dataArray[7]}

echo -e "$PRES\t$TEMP\t$HUM\t$OBTIME\t\t$PRES2\t$TEMP2\t$HUM2\t$OBTIME2\r" >> log.txt
#echo -e "$PRES2\t$TEMP2\t$HUM2\t$OBTIME2" >> log.txt

#echo -e "$PRES\t\t$PRES2" >> log.txt
#echo -e "$TEMP\t\t$TEMP2" >> log.txt
#echo -e "$HUM\t\t$HUM2" >> log.txt
#echo -e "$OBTIME\t\t$OBTIME2" >> log.txt
#echo '' >> log.txt
