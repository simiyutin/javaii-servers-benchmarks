from pandas import read_csv
import glob
import json
import numpy as np
import matplotlib.pyplot as plt


def str2arr(str_row):
    return np.array([int(val) for val in str_row.split(',')[:-1]])


if __name__ == '__main__':

    files = glob.glob("*")
    files.remove("main.py")

    m1xx = []
    m1yy = []

    for file in files:
        part = file.split('_')
        arch = "_".join(part[:2])
        arraySize = int(part[2])
        deltaMillis = int(part[3])
        numberOfRequests = int(part[4])
        numberOfClients = int(part[5])

        print(arch, arraySize, deltaMillis, numberOfRequests, numberOfClients)
        with open(file, 'r') as f:
            m1 = str2arr(f.readline())
            m2 = str2arr(f.readline())
            m3 = str2arr(f.readline())

        m1yy.append(np.max(m1))
        m1xx.append(arraySize) #todo pass varying in the name


    m1xx = np.array(m1xx)
    m1yy = np.array(m1yy)

    sortInd = m1xx.argsort()
    m1xx = m1xx[sortInd]
    m1yy = m1yy[sortInd]

    plt.plot(m1xx, m1yy)
    plt.title('tcp_serial')
    plt.ylabel('mean work time')
    plt.xlabel('array size')
    plt.show()


