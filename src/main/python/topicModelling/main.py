# Topic modelling for JS code
#
# Copyright (C) 2018
# Author: Trung Hieu Tran <trunghieu.tran@utdallas.edu>
# For more information, see README.MD <to be updated>

# from nltk.corpus import stopwords
# from nltk.stem.wordnet import WordNetLemmatizer

# import string
import numpy
import gensim
from gensim import corpora
import os
import sys
### constants
# dir
corpusDir = "data/corpus/"
corpusFile = corpusDir + "trainTM.txt"
inputDir = "data/input/"
outputDir = "data/output/"
snapShotDir = "data/snapShot/"
topicDetailsDir = snapShotDir + "topicDetails/"

topicModelFile = "topicModel.txt"
topicModelDistribution = "topicDistribution.txt"

#file
ldaModelSnapShot = snapShotDir + "ldaModel"
dictionarySnapShot = snapShotDir + "dictionary"

#const
numOfTopic = 100
LDA = gensim.models.ldamodel.LdaModel
###

def clean(doc):
    # stop = set(stopwords.words('english'))
    # exclude = set(string.punctuation)
    # lemma = WordNetLemmatizer()
    #
    # stop_free = " ".join([i for i in doc.lower().split() if i not in stop])
    # punc_free = ''.join(ch for ch in stop_free if ch not in exclude)
    # normalized = " ".join(lemma.lemmatize(word) for word in punc_free.split())

    return doc


def init_Document_Term_Matrix(doc_clean):
    dictionary = corpora.Dictionary(documents=doc_clean)
    doc_term_matrix = [dictionary.doc2bow(doc) for doc in doc_clean]
    return doc_term_matrix, dictionary

def getTopicForQuery (question, dictionary, ldaModel):
    ques_vec = dictionary.doc2bow(question.split())

    topic_vec = ldaModel[ques_vec]

    word_count_array = numpy.empty((len(topic_vec), 2), dtype = numpy.object)

    for i in range(len(topic_vec)):
        word_count_array[i, 0] = topic_vec[i][0]
        word_count_array[i, 1] = topic_vec[i][1]

    idx = numpy.argsort(word_count_array[:, 1])
    idx = idx[::-1]
    word_count_array = word_count_array[idx]
    # print(word_count_array)
    # final = ldaModel.print_topic(word_count_array[0, 0], 10)
    # print(final)
    # question_topic = final.split('*') ## as format is like "probability * topic"

    return word_count_array

def getPredictionsForFileData(filename, dictionary, ldaModel):
    data = getDataFromFile(filename)
    res = getTopicForQuery(question=data, dictionary=dictionary, ldaModel=ldaModel)
    return res

def runPrediction(inputDir, dictionary, ldaModel, outputDir):
    fileList = getAllFilenamesInDirectory(inputDir)
    for filename in fileList:
        print("Predicting : " + filename)
        topicResult = getPredictionsForFileData(filename=inputDir + filename, dictionary=dictionary, ldaModel=ldaModel)
        printOutTopicResult(topicResult, outputDir, filename)
        print("Done predicting : " + filename)

def runPredictionByFile(inputFile, dictionary, ldaModel, outputFile):
    # print("Predicting : " + inputFile)
    topicResult = getPredictionsForFileData(filename=inputFile, dictionary=dictionary, ldaModel=ldaModel)
    printOutTopicResultToFile(topicResult, outputFile=outputFile)

def trainingData(corpusDir):
    fileList = getAllFilenamesInDirectory(dir=corpusDir)
    print(fileList)
    doc_complete = []
    for filename in fileList:
        doc = getDataFromFile(filename=corpusDir + filename)
        doc_complete.append(doc)

    doc_clean = [clean(doc).split() for doc in doc_complete]
    doc_term_matrix, dictionary = init_Document_Term_Matrix(doc_clean)

    ldaModel = LDA(doc_term_matrix, num_topics=numOfTopic, id2word=dictionary)

    saveLdaModelToFile(ldaModel=ldaModel, filename=ldaModelSnapShot)
    saveDictionaryToFile(dictionary=dictionary, filename=dictionarySnapShot)
    saveTopicDetailsToFile(ldaModel, dictionary, numOfTopic, topicDetailsDir)

    print("DONE training")
    return ldaModel, dictionary

def trainingDataFromFile(corpusFile):
    doc = getDataFromFile(filename=corpusFile)
    doc_complete = []
    doc2 = doc.split("\n")
    for i in range(len(doc2)):
        doc_complete.append(doc2[i])

    doc_clean = [clean(doc).split() for doc in doc_complete]
    doc_term_matrix, dictionary = init_Document_Term_Matrix(doc_clean)

    ldaModel = LDA(doc_term_matrix, num_topics=numOfTopic, id2word=dictionary)

    saveLdaModelToFile(ldaModel=ldaModel, filename=ldaModelSnapShot)
    saveDictionaryToFile(dictionary=dictionary, filename=dictionarySnapShot)
    saveTopicDetailsToFile(ldaModel, dictionary, numOfTopic, topicDetailsDir)

    print("DONE training")
    return ldaModel, dictionary


def runPredictionForAllDir(dir, ldaModel, dictionary):
    cc = 0
    cerr = 0
    for x in os.listdir(dir):
        fi = dir + x + "/" + topicModelFile
        fo = dir + x + "/" + topicModelDistribution
        cc += 1
        print("[" + str(cc) + "] " + x)
        try:
            runPredictionByFile(inputFile=fi, ldaModel=ldaModel, dictionary=dictionary, outputFile=fo)
        except:
            print("ERROR")
            cerr +=1
    print("Number of error: " + str(cerr) + "/" + str(cc))


def main(option):
    print("=== START ===")

    if option == 1:
        trainingDataFromFile(corpusFile=corpusFile)

    if option == 2:
        ldaModel = loadLdaModelFromFile(ldaModelSnapShot)
        dictionary = loadDictionaryFromFile(dictionarySnapShot)

        runPrediction(inputDir=inputDir, ldaModel=ldaModel, dictionary=dictionary, outputDir=outputDir)

    if option == 3:
        trainingDataFromFile(corpusFile=corpusFile)
        ldaModel = loadLdaModelFromFile(ldaModelSnapShot)
        dictionary = loadDictionaryFromFile(dictionarySnapShot)
        runPrediction(inputDir=inputDir, ldaModel=ldaModel, dictionary=dictionary, outputDir=outputDir)

    if option == 4:
        ldaModel = loadLdaModelFromFile(ldaModelSnapShot)
        dictionary = loadDictionaryFromFile(dictionarySnapShot)
        runPredictionByFile(inputFile=sys.argv[2], ldaModel=ldaModel, dictionary=dictionary, outputFile=sys.argv[3])

    if option == 5:
        ldaModel = loadLdaModelFromFile(ldaModelSnapShot)
        dictionary = loadDictionaryFromFile(dictionarySnapShot)
        dir = "/home/txt171930/RecoverJSName/resources/parsedData/TestSet/"
        runPredictionForAllDir(dir=dir, ldaModel = ldaModel, dictionary = dictionary)
    print("=== END ===")



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

def printOutTopicResultToFile(topicResult, outputFile):
    f = open(outputFile, 'w')
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
    with open(filename, encoding='utf-8', errors='ignore') as f:
        read_data = f.read()
    f.close()
    return read_data


"""
    Running this file by command:
        >>> python3 main.py [option]
    Where:
        option = 1 for training data from data/corpus/ and build LDA model. Model is stored at data/snapShot/
        option = 2 for prediction data from data/input/ without re-training and output is stored at data/output/
        option = 3 for prediction data from data/input/ after re-training and output is stored at data/output/
        option = 4 for prediction data from input file without re-training and print out to output file
    
"""
if __name__ == "__main__":
    option = int(sys.argv[1])
    # option = 1
    main(option)