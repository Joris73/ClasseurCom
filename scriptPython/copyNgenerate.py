import os
import shutil

ROOT = "C:/Users/Tim/Desktop/Photo/Icones"
OUTPUT = "C:/Users/Tim/Desktop/Photo/_release"
COPY = True

os.chdir(ROOT)
if os.path.isdir(OUTPUT) :
    shutil.rmtree(OUTPUT)
os.mkdir(OUTPUT)
os.mkdir(OUTPUT+"/img")
os.mkdir(OUTPUT+"/img/(576, 576)")
os.mkdir(OUTPUT+"/img/(432, 432)")
os.mkdir(OUTPUT+"/img/(288, 288)")
#listDir = os.listdir()
categorie = ""

def specialCharRemover(string, byNothing) :
    while("_" in string):
        if byNothing :
            string = string.replace("_","")
        else :
            string = string.replace("_"," ")
    while("-" in string) :
        if byNothing :
            string = string.replace("-","")
        else :
            string = string.replace("-","\'")
    return string

def folderParse(folder, copySize) :
    global categorie
    
    string = ""
    os.chdir(ROOT+folder+"/"+copySize)
    listStuff = os.listdir()
    listDir = []
    listFiles = []

    if ('(576, 576)' in listStuff):
        string += folderParse(folder, "(576, 576)")
        folderParse(folder, "(432, 432)")
        folderParse(folder, "(288, 288)")
    else :
        for i in listStuff :
            if ("." in i) :
                if not "desktop" in i :
                    listFiles.append(i)
            else :
                listDir.append(i)
                
    for i in listDir :
        if not("release" in i) :
            string += folderParse(folder+"/"+i,"")

    for i in listFiles :
        if copySize == "(576, 576)" :
            if (i[0] == "_") or (i[1] == "_") :
                filename = i
                while ("-" in filename) :
                    filename = filename.replace("-","")
                nameCategorie = specialCharRemover(i[1:-4], False)
                nameCategorie = nameCategorie[0].upper() + nameCategorie[1:]
                categorie = i[1:-4]
                categorie = "list"+specialCharRemover(categorie, True)
                string += "\nArrayList<Item> "+categorie+" = new ArrayList<Item>();\n"
                string += "listeCategorie.add(new Categorie(\""+nameCategorie+"\", \""+filename[:-4]+"\", "+categorie+"));\n"

    for i in listFiles :
        filename = i
        while ("-" in filename) :
            filename = filename.replace("-","")
        if COPY :
            print("Copying file : "+shutil.copy2(ROOT+folder+"/"+copySize+"/"+i,OUTPUT+"/img/"+copySize+"/"+filename))
        if copySize == "(576, 576)" :
            if not((i[0] == "_") or (i[1] == "_")) :
                name = specialCharRemover(i[:-4], False)
                name = name[0].upper() + name[1:]
                string += categorie+".add(new Item(\""+name+"\",\""+i[:-4]+"\"));\n"

    return string
        
string = folderParse("", "")

f = open(OUTPUT+"/code.java","w")
f.write(string)
f.close()

print("All done.")
