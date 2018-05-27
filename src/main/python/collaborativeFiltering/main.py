# Collaborative Filtering
#
# Copyright (C) 2018
# Author: Trung Hieu Tran <trunghieu.tran@utdallas.edu>
# For more information, see README.MD <to be updated>

import pandas as pd
from collaborativeFiltering.CF import CF

dataDir = "./data/"
matrixFile = dataDir + "matrixData"

def main():
    print("=== START ...")
    # data file
    r_cols = ['varName_id', 'PE_id', 'frequency']
    ratings = pd.read_csv(matrixFile, sep=' ', names=r_cols)
    Y_data = ratings.as_matrix()

    rs = CF(Y_data, k=2, uuCF=1)
    rs.fit()

    rs.print_recommendation()
    print("... END ===")

if __name__ == "__main__":
    # option = int(sys.argv[1])
    # main(option)
    main()