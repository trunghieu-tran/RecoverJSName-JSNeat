# Topic modelling for JS code
#
# Copyright (C) 2018
# Author: Trung Hieu Tran <trunghieu.tran@utdallas.edu>
# For more information, see README.MD <to be updated>

import gensim
from gensim import corpora
import os

def saveLdaModelToFile(ldaModel, filename):
    ldaModel.save(filename)

def loadLdaModelFromFile(filename):
    ldaModel = gensim.models.ldamodel.LdaModel.load(filename)
    return ldaModel

def saveDictionaryToFile(dictionary, filename):
    dictionary.save_as_text(filename)

def loadDictionaryFromFile(filename):
    dictionary = corpora.Dictionary.load_from_text(filename)
    return dictionary

def saveTopicDetailsToFile(ldaModel, dictionary, numOfTopic,  dir):
    for i in range(numOfTopic):
        f = open(dir + str(i), 'w')
        for e in ldaModel.show_topic(i, topn=len(dictionary)):
            f.write(str(e[0]) + " " + str(e[1]) + "\n")
        f.close()

def printOutTopicResult(topicResult, outputDir, filename):
    f = open(outputDir + filename + "_topicProbability", 'w')
    for topic in topicResult:
        f.write(str(topic[0]) + " " + str(topic[1]) + "\n")
    f.close()

def printOutTopicModel(ldaModel, numOfTopic, numOfWords=5):
    for topic in ldaModel.print_topics(num_topics=numOfTopic, num_words=numOfWords):
        print(topic)

def getAllFilenamesInDirectory(dir):
    filenames = []
    for filename in os.listdir(dir):
        filenames.append(filename)
    return filenames

def getDataFromFile(filename):
    with open(filename) as f:
        read_data = f.read()
    f.close()
    return read_data