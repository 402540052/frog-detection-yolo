import firebase_admin
import time
import psutil
from firebase_admin import credentials, firestore, storage

#cred=credentials.Certificate(r'C:\Users\Niu\Desktop\frog-detect-firebase.json')
#firebase_admin.initialize_app(cred, {
    #'storageBucket': 'frog-detect.appspot.com'
#})
cmd = r'''cd C:\Users\Niu\Desktop\darknet-master\build\darknet\x64 & darknet detector test data\voc.data cfg\yolov3-voc.cfg backup\yolov3-voc_16000.weights C:\Users\Niu\Desktop\finish\detect.jpg -dont_show & '''
#db = firestore.client()
#bucket = storage.bucket()
#blob = bucket.blob('detect.jpg')
#blob1 =bucket.blob('result.jpg')
try:
    while True:
        #with open(r'C:\Users\Niu\Desktop\finish\detect.jpg',"wb") as file_obj:
            #blob.download_to_file(file_obj)
        psutil.Popen(cmd,shell=True)
        time.sleep(5)
        #with open(r'C:\Users\Niu\Desktop\darknet-master\build\darknet\x64\predictions.jpg',"rb") as my_file:
            #blob1.upload_from_file(my_file)
except KeyboardInterrupt:
    print ('interrupted!')

