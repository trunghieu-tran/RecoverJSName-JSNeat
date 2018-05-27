import pandas as pd
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from scipy import sparse

class CF(object):

    """
        Y_data: maxtrix of data which is in form of 3 columns, (var.name - PE - frequency)
        k: k- nearest neighbors
        dist_funct: similarity between 2 vector
        uuCF: = 1, for name-name, = 0 for PE-PE
    """
    def __init__(self, Y_data, k, dist_func = cosine_similarity, uuCF = 1):
        self.uuCF = uuCF # = 1, for name-name, = 0 for PE-PE
        self.Y_data = Y_data if uuCF else Y_data[:, [1, 0, 2]]
        self.k = k # number of neighbor points
        self.dist_func = dist_func
        self.Ybar_data = None
        # number of var.names and program entities. Remember to add 1 since id starts from 0
        self.n_varNames = int(np.max(self.Y_data[:, 0])) + 1
        self.n_PEs = int(np.max(self.Y_data[:, 1])) + 1

    def add(self, new_data):
        """
        Update Y_data matrix when new data come.
        For simplicity, suppose that there is no new var.name or program_entity.
        """
        self.Y_data = np.concatenate((self.Y_data, new_data), axis=0)

    def normalize_Y(self):
        names = self.Y_data[:, 0]  # all names - first col of the Y_data
        self.Ybar_data = self.Y_data.copy()
        self.mu = np.zeros((self.n_varNames,))
        for n in range(self.n_varNames):
            # row indices of rating done by var.name
            # since indices need to be integers, we need to convert
            ids = np.where(names == n)[0].astype(np.int32)
            # indices of all frequency associated with var.name n
            item_ids = self.Y_data[ids, 1]
            # and the corresponding frequencies
            frequencies = self.Y_data[ids, 2]
            # take mean
            m = np.mean(frequencies)
            if np.isnan(m):
                m = 0  # to avoid empty array and nan value
            # normalize
            self.Ybar_data[ids, 2] = frequencies - self.mu[n]

        ################################################
        # form the frequency matrix as a sparse matrix. Sparsity is important
        # for both memory and computing efficiency. For example, if #var.name = 1M,
        # #PE = 100k, then shape of the rating matrix would be (100k, 1M),
        # you may not have enough memory to store this. Then, instead, we store
        # nonzeros only, and, of course, their locations.
        self.Ybar = sparse.coo_matrix((self.Ybar_data[:, 2],
                                       (self.Ybar_data[:, 1], self.Ybar_data[:, 0])), (self.n_PEs, self.n_varNames))
        self.Ybar = self.Ybar.tocsr()

    def similarity(self):
        self.S = self.dist_func(self.Ybar.T, self.Ybar.T)

    def refresh(self):
        """
        Normalize data and calculate similarity matrix again (after
        some few frequency added)
        """
        self.normalize_Y()
        self.similarity()

    def fit(self):
        self.refresh()

    def __pred(self, u, i, normalized=1):
        """
        predict the frequency of var.name u for program entity i (normalized)
        if you need the un
        """
        # Step 1: find all var.names who has gone with i
        ids = np.where(self.Y_data[:, 1] == i)[0].astype(np.int32)
        # Step 2:
        users_rated_i = (self.Y_data[ids, 0]).astype(np.int32)
        # Step 3: find similarity btw the current var.name and others
        # who already went with i
        sim = self.S[u, users_rated_i]
        # Step 4: find the k most similarity var.name
        a = np.argsort(sim)[-self.k:]
        # and the corresponding similarity levels
        nearest_s = sim[a]
        # How did each of 'near' var.name went with PE i
        r = self.Ybar[i, users_rated_i[a]]
        if normalized:
            # add a small number, for instance, 1e-8, to avoid dividing by 0
            return (r * nearest_s)[0] / (np.abs(nearest_s).sum() + 1e-8)

        return (r * nearest_s)[0] / (np.abs(nearest_s).sum() + 1e-8) + self.mu[u]

    def pred(self, u, i, normalized=1):
        """
        predict the frequency of var.name u going with i (normalized)
        if you need the un
        """
        if self.uuCF: return self.__pred(u, i, normalized)
        return self.__pred(i, u, normalized)

    def recommend(self, u, normalized=1):
        """
        Determine all PE should be recommended for var.name u. (uuCF =1)
        or all var.name who might go with PE u (uuCF = 0)
        The decision is made based on all i such that:
        self.pred(u, i) > 0. Suppose we are considering PEs which
        have not been going with u yet.
        """
        ids = np.where(self.Y_data[:, 0] == u)[0]
        PEs_going_with_u = self.Y_data[ids, 1].tolist()
        recommended_PEs = []
        recommended_freq = []
        for i in range(self.n_PEs):
            if i not in PEs_going_with_u:
                freq = self.__pred(u, i)
                if freq > 0:
                    recommended_PEs.append(i)
                    recommended_freq.append(freq)

        return recommended_PEs, recommended_freq

    def print_recommendation(self):
        """
        print all PE which should be recommended for each var.name
        """
        print('Recommendation: ')
        for u in range(self.n_varNames):
            recommended_PEs, recommended_freq = self.recommend(u)
            if self.uuCF:
                print('    Recommend PE(s):', recommended_PEs, 'to var.name', u, 'with frequencies', np.around(recommended_freq,2))
            else:
                print('    Recommend PE', u, 'to var.name(s) : ', recommended_PEs, 'with frequencies', np.append(recommended_freq,2))