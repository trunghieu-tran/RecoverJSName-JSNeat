# Topic modelling for JS code
#
# Copyright (C) 2018
# Author: Trung Hieu Tran <trunghieu.tran@utdallas.edu>
# For more information, see README.MD <to be updated>

from nltk.corpus import stopwords
from nltk.stem.wordnet import WordNetLemmatizer

import string, numpy
import sys

from utils import *

### constants
# dir
corpusDir = "data/corpus/"
inputDir = "data/input/"
outputDir = "data/output/"
snapShotDir = "data/snapShot/"
topicDetailsDir = snapShotDir + "topicDetails/"

#file
ldaModelSnapShot = snapShotDir + "ldaModel"
dictionarySnapShot = snapShotDir + "dictionary"

#const
numOfTopic = 5
LDA = gensim.models.ldamodel.LdaModel
###

def clean(doc):
    stop = set(stopwords.words('english'))
    exclude = set(string.punctuation)
    lemma = WordNetLemmatizer()

    stop_free = " ".join([i for i in doc.lower().split() if i not in stop])
    punc_free = ''.join(ch for ch in stop_free if ch not in exclude)
    normalized = " ".join(lemma.lemmatize(word) for word in punc_free.split())

    return normalized


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

def main(option):
    print("=== START ===")

    if option == 1:
        trainingData(corpusDir=corpusDir)

    if option == 2:
        ldaModel = loadLdaModelFromFile(ldaModelSnapShot)
        dictionary = loadDictionaryFromFile(dictionarySnapShot)

        runPrediction(inputDir=inputDir, ldaModel=ldaModel, dictionary=dictionary, outputDir=outputDir)

    if option == 3:
        trainingData(corpusDir=corpusDir)
        ldaModel = loadLdaModelFromFile(ldaModelSnapShot)
        dictionary = loadDictionaryFromFile(dictionarySnapShot)
        runPrediction(inputDir=inputDir, ldaModel=ldaModel, dictionary=dictionary, outputDir=outputDir)

    print("=== END ===")


"""
    Running this file by command:
        >>> python3 main.py [option]
    Where:
        option = 1 for training data from data/corpus/ and build LDA model. Model is stored at data/snapShot/
        option = 2 for prediction data from data/input/ without re-training and output is stored at data/output/
        option = 3 for prediction data from data/input/ after re-training and output is stored at data/output/
    
"""
if __name__ == "__main__":
    option = int(sys.argv[1])
    main(option)