# Collaborative Filtering
#
# Copyright (C) 2018
# Author: Trung Hieu Tran <trunghieu.tran@utdallas.edu>
# For more information, see README.MD <to be updated>

import pandas as pd
import numpy as np
from collaborativeFiltering.CF import CF
from sklearn import cross_validation as cv
from sklearn.metrics.pairwise import pairwise_distances
from sklearn.metrics import mean_squared_error
from math import sqrt


dataDir = "/Users/tranhieu/Research_Projects/RecoverVarNameJS/program/RecoverJSName/resources/parsedData/trainingData/CFInput/"
inputFile = dataDir + "cfInputData"
outputFile = dataDir + "recommendation"


dataDirInternal = "./data/"
inputFileInternal = dataDirInternal + "matrixData"
outputFileInternal = dataDirInternal + "recommendation"

def predict(freq, similarity, type='varName'):
    if type == 'varName':
        mean_varName_freq = freq.mean(axis=1)
        #Use np.newaxis so that mean_varName_freq has same format as freq
        freq_diff = (freq - mean_varName_freq[:, np.newaxis])
        pred = mean_varName_freq[:, np.newaxis] + similarity.dot(freq_diff) / np.array([np.abs(similarity).sum(axis=1)]).T
    elif type == 'peRe':
        pred = freq.dot(similarity) / np.array([np.abs(similarity).sum(axis=1)])
    return pred

def rmse(prediction, ground_truth):
    prediction = prediction[ground_truth.nonzero()].flatten()
    ground_truth = ground_truth[ground_truth.nonzero()].flatten()
    return sqrt(mean_squared_error(prediction, ground_truth))

def main2():
    print("=== START ...")
    # data file
    headers = ['varNameID', 'peReID', 'Frequency']
    records = pd.read_csv(inputFileInternal, sep=' ', names=headers)
    n_varName = records.varNameID.unique().shape[0]
    n_peRe = records.peReID.unique().shape[0]
    print('Number of var.Name = ' + str(n_varName) + ' \nNumber of peRe = ' + str(n_peRe))

    train_data, test_data = cv.train_test_split(records, test_size=0.25)
    train_data_matrix = np.zeros((n_varName, n_peRe))

    for line in train_data.itertuples():
        train_data_matrix[line[1] - 1, line[2] - 1] = line[3]

    test_data_matrix = np.zeros((n_varName, n_peRe))
    for line in test_data.itertuples():
        test_data_matrix[line[1] - 1, line[2] - 1] = line[3]


    varName_similarity = pairwise_distances(train_data_matrix, metric='cosine')
    peRe_similarity = pairwise_distances(train_data_matrix.T, metric='cosine')

    peRe_prediction = predict(train_data_matrix, peRe_similarity, type='peRe')
    varName_prediction = predict(train_data_matrix, varName_similarity, type='varName')
    print('varName-based CF RMSE: ' + str(rmse(varName_prediction, test_data_matrix)))
    print('peRe-based CF RMSE: ' + str(rmse(peRe_prediction, test_data_matrix)))

    print("... END ===")

if __name__ == "__main__":
    # option = int(sys.argv[1])
    # main(option)
    main2()