from PIL import Image
import numpy as np
import matplotlib.pyplot as plt
import os
import random
import shutil

#path_name = 'C:\\Users\\Niu\\Desktop\\finish\\got' #原图片所在路径
xml_path_name = r'C:\Users\Niu\Desktop\project\VOCdevkit\VOC2007\Annotations'
picture_path_name = r'C:\Users\Niu\Desktop\project\VOCdevkit\VOC2007\JPEGImages'
#print(os.listdir(path=xml_path_name))
for item in os.listdir(path=picture_path_name):
    if os.path.splitext(item)[0]+'.xml' not in os.listdir(path=xml_path_name):
        print(item)
        os.remove(os.path.join(picture_path_name,item))
        
        
        '''
        file_path1 = os.path.join(path_name, item)
        file_path2 = os.path.join(picture_path_name, item)
        out_path = os.path.splitext((file_path2))[0] + '.jpg'
        print(out_path)
        shutil.copyfile(file_path1,out_path)
'''
    #Image.open(file_path1).save(out_path)
    #left = random.randint(0,32)
    #upper = random.randint(0,32)
    #Image.open(file_path1).resize((256,256)).save(out_path)
    #Image.open(file_path1).crop((left,upper,(left+224),(upper+224))).save(out_path)
    #Image.open(file_path1).resize((224,224)).save(out_path)
    #Image.open(file_path1).rotate(270).save(out_path)
    
    #transpose(Image.FLIP_LEFT_RIGHT)
    #Image.FLIP_TOP_BOTTOM
    #image_resize.save(out_path)'''

'''for item in os.listdir(path=xml_path_name):
    if item != '.DS_Store':
        file_path1 = os.path.join(path_name, item)
        file_path2 = os.path.join(out_path_name, item)
        out_path = os.path.splitext((file_path2))[0] + ' 4.jpg'
        print(out_path)
        #Image.open(file_path1).save(out_path)
        #left = random.randint(0,32)
        #upper = random.randint(0,32)
        #Image.open(file_path1).resize((256,256)).save(out_path)
        #Image.open(file_path1).crop((left,upper,(left+224),(upper+224))).save(out_path)
        #Image.open(file_path1).resize((224,224)).save(out_path)
        #Image.open(file_path1).rotate(270).save(out_path)
        Image.open(file_path1).transpose(Image.FLIP_TOP_BOTTOM).save(out_path)
        #transpose(Image.FLIP_LEFT_RIGHT)
        #Image.FLIP_TOP_BOTTOM
    #image_resize.save(out_path)'''
print("finish rename")