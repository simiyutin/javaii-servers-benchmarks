import os
if __name__ == '__main__':
    archs = [
        'tcp_serial',
        'tcp_threadperclient',
        'tcp_threadpool',
        'tcp_nonblocking',
        'tcp_async',
        'udp_threadpool',
        'udp_threadperrequest'
    ]

    varD = 'cd .. && java -jar target/bubble_service-1.0-SNAPSHOT-jar-with-dependencies.jar {} 10 D 0 4000 200 N 4000 M 10'
    varM = 'cd .. && java -jar target/bubble_service-1.0-SNAPSHOT-jar-with-dependencies.jar {} 10 M 10 100 10 N 6000 D 1000'
    varN = 'cd .. && java -jar target/bubble_service-1.0-SNAPSHOT-jar-with-dependencies.jar {} 10 N 1000 10000 1000 M 100 D 10'

    for arch in archs:
        print("Now testing {}:".format(arch))
        # os.system(varD.format(arch))
        os.system(varM.format(arch))
