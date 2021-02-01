#!/usr/bin/env python2
import time; 
import sys;

epoch = float(sys.argv[1]);

timeStr = time.strftime("%d %b %Y %H:%M:%S", time.localtime(epoch))
print(timeStr);
