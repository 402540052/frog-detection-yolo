import os
import cv2
import sys

if sys.version_info[0] == 2:
    import xml.etree.cElementTree as ET
else:
    import xml.etree.ElementTree as ET
    
img_folder = 'JPEGImages'
xml_folder = 'Annotations'

count = 0
for file in os.listdir(img_folder):
    img_path = os.path.join(img_folder, file)
    xml_path = os.path.join(xml_folder, file.split('.')[0] + '.xml')
    img = cv2.imread(img_path)
    # image does not exist
    if img is None:
        count += 1
        print(file)
        os.remove(img_path)
        os.remove(xml_path)
        print('Bad images', count)
    else:       
        anno = ET.parse(xml_path).getroot()
        obj = anno.find('object')
        if obj is None:
            count += 1
            print(file)
            os.remove(img_path)
            os.remove(xml_path)
            print('Bad XML', count)

if count == 0:
    print('All clean images')
    