import os
import os.path
import xml.dom.minidom
 
path=r"C:\Users\Niu\Desktop\project\VOCdevkit\VOC2007\ann"
files=os.listdir(path)  #得到文件夹下所有文件名称
for xmlFile in files: #遍历文件夹
    print(xmlFile)
    if not os.path.isdir(xmlFile): #判断是否是文件夹,不是文件夹才打开  
        dom=xml.dom.minidom.parse(os.path.join(path,xmlFile))
        
        root=dom.documentElement
        
        name=root.getElementsByTagName('name')
        #if len(name) == 0:
            #os.remove(os.path.join(path,xmlFile))
        for i in range(len(name)):
            if name[i] .firstChild.data== 'blue mountains tree frog':
                name[i].firstChild.data = 'Blue Mountains Tree Frog'
            elif name[i] .firstChild.data== 'BluePoisonDartFrog':
                name[i].firstChild.data = 'Blue Poison Dart Frog'
            elif name[i] .firstChild.data== 'corroboree frogdd':
                name[i].firstChild.data = 'Corroboree Frog'
            elif name[i] .firstChild.data== 'GreatBarred':
                name[i].firstChild.data = 'Great Barred Frog'
            elif name[i] .firstChild.data== 'GreenAndGoldenBellFrog':
                name[i].firstChild.data = 'Green And Golden Bell Frog'
            elif name[i] .firstChild.data== 'GoldenPoisonDartFrog':
                name[i].firstChild.data = 'Golden Poison Dart Frog'
            elif name[i] .firstChild.data== 'green tree frog':
                name[i].firstChild.data = 'Green Tree Frog'
            elif name[i] .firstChild.data== 'VietnameseMossy':
                name[i].firstChild.data = 'Vietnamese Mossy Forg'
            elif name[i] .firstChild.data== 'RedCrownedToadlet':
                name[i].firstChild.data = 'Red Crowned Toadlet'
            elif name[i] .firstChild.data== 'RedEyed':
                name[i].firstChild.data = 'Red Eyed Tree Frog'
    #将修改后的xml文件保存
    with open(os.path.join(path,xmlFile), 'w',encoding='utf-8') as fh:
        dom.writexml(fh)
        print('写入name/pose OK!')

    

print("finish")



