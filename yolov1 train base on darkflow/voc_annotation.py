import xml.etree.ElementTree as ET
from os import getcwd

sets=[('2007', 'train'), ('2007', 'val'), ('2007', 'test')]

classes = ['BluePoisonDartFrog',
            'CaneToad',
            'GoldenPoisonDartFrog',
            'GreatBarred',
            'GreenAndGoldenBellFrog',
            'RedCrownedToadlet',
            'RedEyed',
            'VietnameseMossy',
            'blue mountains tree frog',
            'corroboree frogdd',
            'green tree frog']
            
def convert_annotation(year, image_id, list_file):
    in_file = open('Dataset/VOC%s/Annotations/%s.xml'%(year, image_id),encoding='latin-1')
    tree=ET.parse(in_file)
    root = tree.getroot()

    for obj in root.iter('object'):
        difficult = obj.find('difficult').text
        cls = obj.find('name').text
        if cls not in classes or int(difficult)==1:
            continue
        cls_id = classes.index(cls)
        xmlbox = obj.find('bndbox')
        b = (int(xmlbox.find('xmin').text), int(xmlbox.find('ymin').text), int(xmlbox.find('xmax').text), int(xmlbox.find('ymax').text))
        list_file.write(" " + ",".join([str(a) for a in b]) + ',' + str(cls_id))

wd = getcwd()

for year, image_set in sets:
    image_ids = open('Dataset/VOC%s/ImageSets/Main/%s.txt'%(year, image_set)).read().strip().split('\n')
    #print(image_ids)
    list_file = open('%s.txt'%(image_set), 'w')
    for image_id in image_ids:
        # try:
            print(image_id, end='\r')
            list_file.write('%s/VOCdevkit/VOC%s/JPEGImages/%s.jpg'%(wd, year, image_id))
            convert_annotation(year, image_id, list_file)

            list_file.write('\n')
        # except:
        #     print(image_id)
        #     pass
    list_file.close()

