#!/bin/bash
dir=`pwd`
cd $dir
cd ../python/topicModelling/
input=$1
output=$2
python3 main.py 4 $input $output
echo "DONE prediction $input"