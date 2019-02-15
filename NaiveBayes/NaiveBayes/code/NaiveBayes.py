import sys
from scipy.sparse import csr_matrix
import numpy as np
from Eval import Eval
from math import log, exp
import time
from imdb import IMDBdata

class NaiveBayes:
    def __init__(self, data, alpha):
        self.ALPHA = alpha
        self.data = data # training data
        #TODO: Initalize parameters
        self.vocab_len = data.vocab.GetVocabSize()
        self.count_positive = 0
        self.count_negative = 0
        self.num_positive_reviews = 0
        self.num_negative_reviews = 0
        self.total_positive_words = 0
        self.total_negative_words = 0
        self.P_positive = 0
        self.P_negative = 0
        self.deno_pos = 1.0
        self.deno_neg = 1.0
        self.Train(data.X, data.Y)

    # Train model - X are instances, Y are labels (+1 or -1)
    # X and Y are sparse matrices
    def Train(self, X, Y):
        #TODO: Estimate Naive Bayes model parameters
        positive_indices = np.argwhere(Y == 1.0).flatten()
        negative_indices = np.argwhere(Y == -1.0).flatten()

        self.num_positive_reviews = positive_indices.size
        self.num_negative_reviews = negative_indices.size

        self.count_positive = np.zeros([1, X.shape[1]])
        self.count_negative = np.zeros([1, X.shape[1]])

        self.total_positive_words = 0
        self.total_negative_words = 0

        wordID = X.indices
        wordCount = X.data
        docPos = X.indptr
        docID = -1
        pos = 0

        # Count word
        while pos < wordID.size:
            if docID + 1 < docPos.size and pos == docPos[docID + 1]:
                docID += 1
            if Y[docID] == 1.0:
                self.count_positive[0, wordID[pos]] += wordCount[pos]
                self.total_positive_words += wordCount[pos]
            elif Y[docID] == -1.0:
                self.count_negative[0, wordID[pos]] += wordCount[pos]
                self.total_negative_words += wordCount[pos]
            pos += 1

        self.deno_pos = self.total_positive_words + self.vocab_len * self.ALPHA
        self.deno_neg = self.total_negative_words + self.vocab_len * self.ALPHA

        return

    def PrintWeight(self):
        pos_weight = np.zeros([1, self.count_positive.size])
        neg_weight = np.zeros([1, self.count_negative.size])

        pos = 0
        while pos < self.count_positive.size:
            pos_weight[0, pos] = log((self.count_positive[0, pos] + self.ALPHA) / self.deno_pos) - log((self.count_negative[0, pos] + self.ALPHA) / self.deno_pos)
            neg_weight[0, pos] = log((self.count_negative[0, pos] + self.ALPHA) / self.deno_pos) - log((self.count_positive[0, pos] + self.ALPHA) / self.deno_neg)
            pos += 1

        # Get top 20 word
        a = np.argpartition(pos_weight[0, ], -20)[-20:]
        b = np.argpartition(neg_weight[0, ], -20)[-20:]
        top20_pos = np.array([a, pos_weight[0, a]])
        top20_neg = np.array([b, neg_weight[0, b]])

        index1 = np.lexsort((-top20_pos[0, ], -top20_pos[1, ]))
        index2 = np.lexsort((-top20_neg[0, ], -top20_neg[1, ]))

        print("Top 20 positive words:")
        for i in index1:
            print("%s_%f" % (self.data.vocab.GetWord(top20_pos[0, i]), top20_pos[1, i]), end=' ')
        print()
        print("Top 20 negative words:")
        for i in index2:
            print("%s_%f" % (self.data.vocab.GetWord(top20_neg[0, i]), top20_neg[1, i]), end=' ')
        print()
        return

    # Predict labels for instances X
    # Return: Sparse matrix Y with predicted labels (+1 or -1)
    def PredictLabel(self, X):
        #TODO: Implement Naive Bayes Classification
        pred_labels = []
        sh = X.shape[0]
        for i in range(sh):
            self.P_positive = log(self.num_positive_reviews / (self.num_positive_reviews + self.num_negative_reviews))
            self.P_negative = log(self.num_negative_reviews / (self.num_positive_reviews + self.num_negative_reviews))

            z = X[i].nonzero()
            for j in range(len(z[0])):
                # Look at each feature
                index = z[1][j]
                self.P_positive += log((self.count_positive[0, index] + self.ALPHA) / self.deno_pos)
                self.P_negative += log((self.count_negative[0, index] + self.ALPHA) / self.deno_neg)
            if self.P_positive > self.P_negative:            # Predict positive
                pred_labels.append(1.0)
            else:               # Predict negative
                pred_labels.append(-1.0)

        return pred_labels

    def LogSum(self, logx, logy):
        # TO DO: Return log(x+y), avoiding numerical underflow/overflow.
        m = max(logx, logy)
        return m + log(exp(logx - m) + exp(logy - m))

    # Predict the probability of each indexed review in sparse matrix text
    # of being positive
    # Prints results
    def PredictProb(self, test, indexes, probThresh):
        resultP = [0,0,0,0] # TP, TN, FP, FN
        resultN = [0, 0, 0, 0]
        for i in indexes:
            # TO DO: Predict the probability of the i_th review in test being positive review
            # TO DO: Use the LogSum function to avoid underflow/overflow
            z = test.X[i].nonzero()
            predicted_label = 0
            predicted_prob_positive = log(self.num_positive_reviews / (self.num_positive_reviews + self.num_negative_reviews))
            predicted_prob_negative = log(self.num_negative_reviews / (self.num_positive_reviews + self.num_negative_reviews))
            for j in range(len(z[0])):
                col_index = z[1][j]

                predicted_prob_positive += log((self.count_positive[0, col_index] + self.ALPHA) / self.deno_pos)
                predicted_prob_negative += log((self.count_negative[0, col_index] + self.ALPHA) / self.deno_neg)

            sum_positive = exp(predicted_prob_positive - self.LogSum(predicted_prob_positive, predicted_prob_negative))
            sum_negative = exp(predicted_prob_negative - self.LogSum(predicted_prob_positive, predicted_prob_negative))

            if sum_positive > sum_negative and sum_positive > probThresh:
                predicted_label = 1.0
            else:
                predicted_label = -1.0

            if test.Y[i] == 1.0:
                if test.Y[i] == predicted_label:
                    resultP[0] += 1
                    resultN[1] += 1
                else:
                    resultP[3] += 1
                    resultN[2] += 1
            else:
                if test.Y[i] == predicted_label:
                    resultP[1] += 1
                    resultN[0] += 1
                else:
                    resultP[2] += 1
                    resultN[3] += 1
            # TO DO: Comment the line above, and uncomment the line below
            print(test.Y[i], predicted_label, sum_positive, sum_negative, test.X_reviews[i])

        print("Prediction Precision of Positive: ", self.EvalPrecision(resultP))
        print("Prediction Recall of Positive: ", self.EvalRecall(resultP))
        print("Prediction Precision of Negative: ", self.EvalPrecision(resultN))
        print("Prediction Recall of Negative: ", self.EvalRecall(resultN))
        return

    # Evaluate performance on test data
    def Eval(self, test):
        Y_pred = self.PredictLabel(test.X)
        ev = Eval(Y_pred, test.Y)
        return ev.Accuracy()

    def EvalPrecision(self, result):
        return result[0]/(result[0] + result[2])

    def EvalRecall(self,result):
        return result[0]/(result[0] + result[3])

if __name__ == "__main__":
    
    print("Reading Training Data")
    traindata = IMDBdata("%s/train" % sys.argv[1])
    print("Reading Test Data")
    testdata  = IMDBdata("%s/test" % sys.argv[1], vocab=traindata.vocab)    
    print("Computing Parameters")
    nb = NaiveBayes(traindata, float(sys.argv[2]))
    nb.PrintWeight()
    print("Evaluating")
    print("Test Accuracy: ", nb.Eval(testdata))
    nb.PredictProb(testdata, range(testdata.X.shape[0]), 0.95)

