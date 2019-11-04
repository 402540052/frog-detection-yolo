#_*_coding:UTF-8_*_

# 注意修改JPG folder名

import os
import sys
from xml.etree import ElementTree as ET

imageNames = []
xmlNames = []
tags = []
file_path = ''
def getImageNames(file_dir):
    for root, dirs, files in os.walk(file_dir):
        imageNames.extend(files)
    print(imageNames)

def printNames(names):
    for name in names:
        print(name)
def getXmlNames(file_dir):
    for root, dirs, files in os.walk(file_dir):
        xmlNames.extend(files)
        matchCheck()

def matchCheck():
    print('\nchecking xml and image mismatch cases...')
    print("\nnot found xml files:")
    for image in imageNames:
        if not ((image.replace('.jpg', '.xml') in xmlNames) or (image.replace('.bmp', '.xml') in xmlNames)):
            print(image)
    print("\nnot found image files:")
    for xml in xmlNames:
        if not ((xml.replace('.xml', '.jpg') in imageNames) or (xml.replace('.xml', '.bmp') in imageNames)):
            print(xml)
           # os.remove(xml)
    print("\ninvalid tag files:")
    for xml in xmlNames:
        pass
        #xmlTagNameCheck(file_path + "/" + folder_annoation +"/" + xml)
def xmlTagNameCheck(file_dir):
    tree = ET.parse(file_dir)
    root = tree.getroot()
    childs = list(root)
    for child in childs:
        if(child.tag == 'object'):
            if not (list(child)[0].text in tags):
                print('invalid tag [' + list(child)[0].text + '] found in: ' + file_dir)

folder_annoation = "Annotations"
folder_image = "JPEGImages"
def main():
    if (len(sys.argv) > 2):
        print('2')
        global tags
        tags = sys.argv[2].split(',')
    else:
        tags = ['Oil_Pipe', 'EDS_Line', 'person', 'callphone']
    if len(sys.argv) > 1:
        print('1')
        global file_path
        file_path = sys.argv[1]
        print(file_path)
        getImageNames(os.path.join(file_path,folder_image))
        getXmlNames(os.path.join(file_path,folder_annoation)) 
    else:
        print("please input file path.")
        getImageNames(folder_image)
        getXmlNames(folder_annoation)
if __name__ == '__main__':
    main()
