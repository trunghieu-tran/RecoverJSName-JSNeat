# Collaborative Filtering
#
# Copyright (C) 2018
# Author: Trung Hieu Tran <trunghieu.tran@utdallas.edu>
# For more information, see README.MD <to be updated>

import pandas as pd
from collaborativeFiltering.CF import CF

dataDir = "/Users/tranhieu/Research_Projects/RecoverVarNameJS/program/RecoverJSName/resources/parsedData/trainingData/CFInput/"
inputFile = dataDir + "cfInputData"
outputFile = dataDir + "recommendation"


dataDirInternal = "./data/"
inputFileInternal = dataDirInternal + "cfInputData"
outputFileInternal = dataDirInternal + "recommendation"

def main():
    print("=== START ...")
    # data file
    r_cols = ['VarName_id', 'PeRe_id', 'Frequency']
    ratings = pd.read_csv(inputFileInternal, sep=' ', names=r_cols)
    Y_data = ratings.as_matrix()

    rs = CF(Y_data, k=2, uuCF=1)
    rs.fit()

    rs.print_recommendation_to_File(outputFileInternal)
    print("... END ===")

if __name__ == "__main__":
    # option = int(sys.argv[1])
    # main(option)
    main()