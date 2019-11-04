Frog Detection in Android With YOLO
=
App with embedded model
-
This file include the code about the app with embedded model
Open it with Android Studio, after building it can be run directly
mAP is 64%

The interface refers to https://github.com/ShaofengZou/Android-Style-Transfer

App with server-based model
-
This file include the code about the app with server-based model
Open it with Android Studio, after building it can be run directly
But if you want to detect the frog, you should run the code in server first

Caculate mAP
-
Code to caculate mAP, it should be executed by python2

Reference:http://bbs.cvmart.net/articles/199/yolov3-map-ji-suan-jiao-cheng

Code for prepare dataset
-
Code to prepare the dataset, data augmentation include image processing
bulid dataset is used to divied data to train set, test set , val set
convert_xml_to_txt is used to convert xml label to txt which was used in yolo
replace is used to replace the label name in xml

convert_xml_to_txt reference:https://github.com/pjreddie/darknet/blob/master/scripts/voc_label.py

Google-images-download-master
-
Use those code to crawl image from google
Reference:https://github.com/hardikvasa/google-images-download

Server
-
Used to detect the frog as server

Yolov1 train base on darkflow
-
used to train yolov1-tiny model
you can use "flow --model cfg/yolo-new.cfg --load bin/tiny-yolo.weights --train --gpu 1.0" to train the model

Reference:https://github.com/thtrieu/darkflow

Yolov3 train base on darknet
-
used to train yolov3 model

Enter x64 folder and you can use "darknet.exe detector train cfg/voc.data cfg/yolov3-voc.cfg darknet53.conv.74" to train the model

Reference:https://github.com/AlexeyAB/darknet

SSD train
-
used to train SSD model
you can use "python train.py" to train the model

Reference:https://github.com/amdegroot/ssd.pytorch
