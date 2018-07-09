#!/bin/bash
dir=`pwd`
echo "$dir"
cd $dir
cd ../python/topicModelling/
python3 main.py 1
echo "DONE run bashscript TopicModelTraining"