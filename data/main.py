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

    metricsNames = {
        'm1': 'mean client work time',
        'm2': 'mean server serve time',
        'm3': 'mean server sort time',
    }

    optNames = {
        'N': 'array size',
        'D': 'client delta',
        'M': 'number of clients'
    }

    data = dict()

    for file in files:
        part = file.split('_')
        arch = "_".join(part[:2])
        varyingOpt = part[2]
        opts = dict()
        arraySize = int(part[3])  # N
        opts['N'] = arraySize
        deltaMillis = int(part[4])  # D
        opts['D'] = deltaMillis
        numberOfRequests = int(part[5])
        numberOfClients = int(part[6])  # M
        opts['M'] = numberOfClients

        with open(file, 'r') as f:
            m1 = str2arr(f.readline())
            m2 = str2arr(f.readline())
            m3 = str2arr(f.readline())

        metrics = data.get(varyingOpt, dict())
        # todo remove duplication
        graph = metrics.get('m1', dict())
        grapharch = graph.get(arch, {'xx': [], 'yy': []})
        grapharch['yy'].append(np.mean(m1)) # todo mean or max or smth else?
        grapharch['xx'].append(opts[varyingOpt])
        graph[arch] = grapharch
        metrics['m1'] = graph
        graph = metrics.get('m2', dict())
        grapharch = graph.get(arch, {'xx': [], 'yy': []})
        grapharch['yy'].append(np.mean(m2))
        grapharch['xx'].append(opts[varyingOpt])
        graph[arch] = grapharch
        metrics['m2'] = graph
        graph = metrics.get('m3', dict())
        grapharch = graph.get(arch, {'xx': [], 'yy': []})
        grapharch['yy'].append(np.mean(m3))
        grapharch['xx'].append(opts[varyingOpt])
        graph[arch] = grapharch
        metrics['m3'] = graph
        data[varyingOpt] = metrics

    index = 1
    legendInitialized = False
    for varyingOpt, metrics in data.items():
        for metric, graph in metrics.items():
            x = index / 3
            y = index % 3
            plt.subplot(3, 3, index)
            legendHandles = []
            for arch, values in graph.items():
                xx = np.array(values['xx'])
                yy = np.array(values['yy'])
                sortInd = xx.argsort()
                xx = xx[sortInd]
                yy = yy[sortInd]
                archline = plt.plot(xx, yy, label=arch)
                legendHandles.append(archline[0])
                plt.ylabel(metricsNames[metric])
                plt.xlabel(optNames[varyingOpt])

            if not legendInitialized:
                plt.legend(handles=legendHandles, bbox_to_anchor=(1.0, 1.05))
                legendInitialized = True

            index += 1

    plt.tight_layout()
    plt.show()


