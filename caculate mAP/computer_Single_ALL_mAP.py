from voc_eval import voc_eval

import os

current_path = os.getcwd()
results_path = current_path+"/results"
sub_files = os.listdir(results_path)

mAP = []
for i in range(len(sub_files)):
    class_name = sub_files[i].split(".txt")[0]
    rec, prec, ap = voc_eval(r'C:\Users\Niu\Desktop\darknet-master\build\darknet\x64\results\{}.txt', r'C:\Users\Niu\Desktop\project\VOCdevkit\VOC2007\ann\{}.xml',r'C:\Users\Niu\Desktop\project\VOCdevkit\VOC2007\ImageSets\Main\test.txt', class_name, '.')
    print("{} :\t {} ".format(class_name, ap))
    mAP.append(ap)

mAP = tuple(mAP)

print("***************************")
print("mAP :\t {}".format( float( sum(mAP)/len(mAP)) )) 