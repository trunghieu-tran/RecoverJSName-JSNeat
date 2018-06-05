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

def main():
    print("=== START ...")
    # data file
    headers = ['VarName_id', 'PeRe_id', 'Frequency']
    ratings = pd.read_csv(inputFileInternal, sep=' ', names=headers)
    Y_data = ratings.as_matrix()

    rs = CF(Y_data, k=2, uuCF=0)
    rs.fit()

    # rs.print_recommendation_to_File(outputFileInternal) 2.37 4.71 2.64
    rs.print_recommendation()
    print("... END ===")

if __name__ == "__main__":
    # option = int(sys.argv[1])
    # main(option)
    main()