# Collaborative Filtering
#
# Copyright (C) 2018
# Author: Trung Hieu Tran <trunghieu.tran@utdallas.edu>
# For more information, see README.MD <to be updated>

import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from scipy import sparse

class CF(object):
    """
        Y_data: maxtrix of data which is in form of 3 columns, (var.name - PE - frequency)
        k: k- nearest neighbors
        dist_funct: function calculates similarity between 2 vector
        uuCF: = 1, for name-name,
              = 0, for PE-PE
    """
    def __init__(self, Y_data, k, dist_func = cosine_similarity, uuCF = 1):
        self.uuCF = uuCF
        self.Y_data = Y_data if uuCF else Y_data[:, [1, 0, 2]]
        self.k = k
        self.dist_func = dist_func

        self.Ybar_data = None
        # number of var.names and program entities. Remember to add 1 since id starts from 0
        self.n_varNames = int(np.max(self.Y_data[:, 0])) + 1
        self.n_PEs = int(np.max(self.Y_data[:, 1])) + 1