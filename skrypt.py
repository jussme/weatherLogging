#!/usr/bin/env python

import re;
import requests;
import os;

def createFile(filePath):
    if not os.path.isfile(filePath):
        try:
            open(filePath, "ax");
        except:
            print("Cannot create file");
            exit(1);
    

class API:
    def __init__(self):
        self.name = 'not given';
        self.requestURL = '';
        self.dataRegexPairs = [];
    
    def toString(self):
        returnString = self.name + ":\n" + self.requestURL + "\ndataRegexPairs:";
        for dataRegexPair in self.dataRegexPairs:
            returnString = returnString + "\n\t" + dataRegexPair[0] + ": " + dataRegexPair[1];
        return returnString;
        
apiList = [];

lon = 1.5;
lat = 1.5;
with open('coords.txt') as file:
    match = re.match("lon[ \t]+(.*)\r?\n?lat[ \t]+(.*)", file.read());
    lon = match.group(1);
    lat = match.group(2);

with open('apiKeys.txt') as file:
    for match in re.finditer("([^\ \t]+)[ \t]([^\ \t]+)[ \t]([^\ \t]+)[ \t]([^\r\n]+(?:\r|\r\n|\n))", file.read(), re.S):
        apiBuffer = API();
        apiBuffer.name = match.group(1);
        apiBuffer.requestURL = match.group(2).replace("LON", lon).replace("LAT", lat);
        apiBuffer.requestURL = apiBuffer.requestURL.replace("APIKEY", match.group(3));
        
        for dataRegexPair in re.finditer('(\w+)[ \t]+([^ \t\n\r]+)', match.group(4)):
            apiBuffer.dataRegexPairs.append((dataRegexPair.group(1), dataRegexPair.group(2)));

        apiList.append(apiBuffer);

if not os.path.isdir("./logs"):
        try:
            os.mkdir("logs");
        except:
            print("Cannot create log directory");
            exit(1);

for api in apiList:
    response = requests.get(api.requestURL).text;
    if not response:
        print('Server request failed');
        exit(1);
    
    #creating one line of data
    dataLine = '';
    for dataRegexPair in api.dataRegexPairs:
        regex = dataRegexPair[1];
        data = re.search(regex, response).group(1);
        if not data:
            print("Data extraction from JSON error\n\n" + api.name + ":\n" + response + "\n\nregex: " + regex);
            exit(1);
        
        dataLine = dataLine + str(round(float(data), 1)) + "\t";
    dataLine = dataLine + "\r\n";
    
    #creating one line representing properties types, ie. "temperature\thumidity\t..."
    dataTemplateLine = '';
    for dataRegexPair in api.dataRegexPairs:
        dataType = dataRegexPair[0];
        dataTemplateLine = dataTemplateLine + dataType + "\t";
    dataTemplateLine = dataTemplateLine + "\r\n";
    
    createFile("./logs/" + api.name + "Template.txt");
    createFile("./logs/" + "log"+api.name+".txt");
    
    with open("./logs/" + api.name + "Template.txt", "at") as templateFile:
        templateFile.write(dataTemplateLine);
    
    with open("./logs/" + "log"+api.name+".txt", "at") as logFile:
        logFile.write(dataLine);
        
    print("|"+api.name+"|");